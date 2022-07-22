package com.github.apetrelli.scafa.sync.http.server.statics;

import static com.github.apetrelli.scafa.http.HttpCodes.BAD_REQUEST;
import static com.github.apetrelli.scafa.http.HttpCodes.METHOD_NOT_ALLOWED;
import static com.github.apetrelli.scafa.http.HttpCodes.NOT_FOUND;
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
import static com.github.apetrelli.scafa.http.HttpHeaders.OK;
import static com.github.apetrelli.scafa.http.HttpHeaders.SCAFA;
import static com.github.apetrelli.scafa.http.HttpHeaders.SERVER;

import java.nio.file.Path;
import java.util.Map;

import com.github.apetrelli.scafa.http.HttpCodes;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.HttpUtils;
import com.github.apetrelli.scafa.proto.io.FlowBuffer;
import com.github.apetrelli.scafa.proto.util.AsciiString;
import com.github.apetrelli.scafa.sync.http.HttpSyncSocket;
import com.github.apetrelli.scafa.sync.http.server.HttpServer;
import com.github.apetrelli.scafa.sync.http.server.impl.HttpServerHandlerSupport;

public class StaticHttpServerHandler extends HttpServerHandlerSupport {

	private static final AsciiString NO_PATH_TRAVERSAL = new AsciiString("No path traversal");

	private static final AsciiString ONLY_GET_ALLOWED = new AsciiString("Only GET allowed");

	private final String basePath;
	
	private final AsciiString basePathAscii;
	
	private final AsciiString basePathSlash;
	
	private final Map<String, Path> localResource2path;

	private final String indexResource;

	private final Map<String, AsciiString> mimeTypeConfig;

	private final HttpServer server;
	
	private final FlowBuffer writeBuffer;

	public StaticHttpServerHandler(HttpSyncSocket<HttpResponse> channel, String basePath, Map<String, Path> localResource2path,
			String indexResource, Map<String, AsciiString> mimeTypeConfig, HttpServer server) {
		super(channel);
		this.basePath = basePath;
		basePathAscii = new AsciiString(basePath);
		basePathSlash = new AsciiString(basePath + "/"); // NOSONAR
		this.localResource2path = localResource2path;
		this.indexResource = indexResource;
		this.mimeTypeConfig = mimeTypeConfig;
		this.server = server;
		writeBuffer = new FlowBuffer(16384);
	}
	
	@Override
	public void onRequestEnd(HttpRequest request) {
		if (GET.equals(request.getMethod())) {
			if (request.getResource().startsWith(basePathAscii)) {
				String localResource = request.getParsedResource().getResource().substring(basePath.length());
				if (localResource.isEmpty() && !basePath.equals("/")) {
					HttpResponse response = createSimpleResponse(HttpCodes.FOUND, FOUND);
					response.headers().setHeader(LOCATION, basePathSlash);
					server.response(channel, response);
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
							HttpResponse response = new HttpResponse(HTTP_1_1, HttpCodes.OK, OK);
							response.headers().setHeader(SERVER, SCAFA);
							response.headers().setHeader(DATE, HttpUtils.getCurrentHttpDate());
							AsciiString connection = request.headers().getHeader(CONNECTION);
							if (!CLOSE_CONNECTION.equals(connection)) {
								connection = KEEP_ALIVE;
							}
							response.headers().setHeader(CONNECTION, connection);
							int dotPosition = localResource.lastIndexOf('.');
							if (dotPosition >= 0) {
								AsciiString contentType = mimeTypeConfig.get(localResource.substring(dotPosition + 1).toLowerCase());
								if (contentType != null) {
									response.headers().setHeader(CONTENT_TYPE, contentType);
								}
							}
							server.response(channel, response, path, writeBuffer);
						} else {
							sendSimpleMessage(NOT_FOUND, new AsciiString("The resource " + localResource + " does not exist"));
						}
					} else {
						sendSimpleMessage(BAD_REQUEST, NO_PATH_TRAVERSAL);
					}
				}
			} else {
				sendSimpleMessage(NOT_FOUND, new AsciiString("Resource " + request.getResource() +  " not found"));
			}
		} else {
			sendSimpleMessage(METHOD_NOT_ALLOWED, ONLY_GET_ALLOWED);
		}
	}

	private void sendSimpleMessage(AsciiString httpCode, AsciiString message) {
		HttpResponse response = createSimpleResponse(httpCode, message);
		server.response(channel, response);
	}

	private HttpResponse createSimpleResponse(AsciiString httpCode, AsciiString message) {
		HttpResponse response = new HttpResponse(HTTP_1_1, httpCode, message);
		response.headers().setHeader(SERVER, SCAFA);
		response.headers().setHeader(CONTENT_LENGTH, CONTENT_LENGTH_0);
		return response;
	}
}
