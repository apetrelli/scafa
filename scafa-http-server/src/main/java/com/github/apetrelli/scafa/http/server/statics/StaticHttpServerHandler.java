package com.github.apetrelli.scafa.http.server.statics;

import static com.github.apetrelli.scafa.http.HttpHeaders.CLOSE_CONNECTION;
import static com.github.apetrelli.scafa.http.HttpHeaders.CONNECTION;
import static com.github.apetrelli.scafa.http.HttpHeaders.CONTENT_LENGTH;
import static com.github.apetrelli.scafa.http.HttpHeaders.CONTENT_LENGTH_0;
import static com.github.apetrelli.scafa.http.HttpHeaders.CONTENT_TYPE;
import static com.github.apetrelli.scafa.http.HttpHeaders.DATE;
import static com.github.apetrelli.scafa.http.HttpHeaders.FOUND;
import static com.github.apetrelli.scafa.http.HttpHeaders.GET;
import static com.github.apetrelli.scafa.http.HttpHeaders.HTTP_1_1;
import static com.github.apetrelli.scafa.http.HttpHeaders.KEEP_ALIVE;
import static com.github.apetrelli.scafa.http.HttpHeaders.LOCATION;
import static com.github.apetrelli.scafa.http.HttpHeaders.SCAFA;
import static com.github.apetrelli.scafa.http.HttpHeaders.SERVER;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.HttpUtils;
import com.github.apetrelli.scafa.http.server.HttpServer;
import com.github.apetrelli.scafa.http.server.impl.HttpServerHandlerSupport;
import com.github.apetrelli.scafa.proto.util.AsciiString;

public class StaticHttpServerHandler extends HttpServerHandlerSupport {

	private static final AsciiString NO_PATH_TRAVERSAL = new AsciiString("No path traversal");

	private static final AsciiString ONLY_GET_ALLOWED = new AsciiString("Only GET allowed");

	private String basePath;
	
	private AsciiString basePathAscii;
	
	private AsciiString basePathSlash;

	private String indexResource;

	private Map<String, AsciiString> mimeTypeConfig;

	private HttpServer server;
	
	private Map<String, Path> localResource2path;

	public StaticHttpServerHandler(HttpAsyncSocket<HttpResponse> channel, String basePath, Map<String, Path> localResource2path,
			String indexResource, Map<String, AsciiString> mimeTypeConfig, HttpServer server) {
		super(channel);
		this.basePath = basePath;
		basePathAscii = new AsciiString(basePath);
		basePathSlash = new AsciiString(basePath + "/"); // NOSONAR
		this.localResource2path = localResource2path;
		this.indexResource = indexResource;
		this.mimeTypeConfig = mimeTypeConfig;
		this.server = server;
	}
	
	@Override
	public CompletableFuture<Void> onRequestEnd(HttpRequest request) {
		if (GET.equals(request.getMethod())) {
			if (request.getResource().startsWith(basePathAscii)) {
				String localResource = request.getParsedResource().getResource().substring(basePath.length());
				if (localResource.isEmpty() && !basePath.equals("/")) {
					HttpResponse response = createSimpleResponse(302, FOUND);
					response.setHeader(LOCATION, basePathSlash);
					return server.response(channel, response, writeBuffer);
				} else {
					while (localResource.startsWith("/")) {
						localResource = localResource.substring(1);
					}
					if (localResource.isEmpty()) {
						localResource = indexResource;
					}
					if (!localResource.contains("..")) {
						Path path = localResource2path.get(localResource);
						if (path != null) {
							HttpResponse response = new HttpResponse(HTTP_1_1, 200, FOUND);
							response.setHeader(SERVER, SCAFA);
							response.setHeader(DATE, HttpUtils.getCurrentHttpDate());
							AsciiString connection = request.getHeader(CONNECTION);
							if (!CLOSE_CONNECTION.equals(connection)) {
								connection = KEEP_ALIVE;
							}
							response.setHeader(CONNECTION, connection);
							int dotPosition = localResource.lastIndexOf('.');
							if (dotPosition >= 0) {
								AsciiString contentType = mimeTypeConfig.get(localResource.substring(dotPosition + 1).toLowerCase());
								if (contentType != null) {
									response.setHeader(CONTENT_TYPE, contentType);
								}
							}
							return server.response(channel, response, path, writeBuffer);
						} else {
							return sendSimpleMessage(404, "The resource " + localResource + " does not exist");
						}
					} else {
						return sendSimpleMessage(400, NO_PATH_TRAVERSAL);
					}
				}
			} else {
				return sendSimpleMessage(404, "Resource " + request.getResource() +  " not found");
			}
		} else {
			return sendSimpleMessage(405, ONLY_GET_ALLOWED);
		}
	}

	private CompletableFuture<Void> sendSimpleMessage(int httpCode, String message) {
		return sendSimpleMessage(httpCode, new AsciiString(message));
	}

	private CompletableFuture<Void> sendSimpleMessage(int httpCode, AsciiString message) {
		HttpResponse response = createSimpleResponse(httpCode, message);
		return server.response(channel, response, writeBuffer);
	}

	private HttpResponse createSimpleResponse(int httpCode, AsciiString message) {
		HttpResponse response = new HttpResponse(HTTP_1_1, httpCode, message);
		response.setHeader(SERVER, SCAFA);
		response.setHeader(CONTENT_LENGTH, CONTENT_LENGTH_0);
		return response;
	}
}
