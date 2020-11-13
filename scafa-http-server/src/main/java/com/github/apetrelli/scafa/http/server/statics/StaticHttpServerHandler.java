package com.github.apetrelli.scafa.http.server.statics;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.server.HttpServer;
import com.github.apetrelli.scafa.http.server.impl.HttpServerHandlerSupport;

public class StaticHttpServerHandler extends HttpServerHandlerSupport {

	private static String getCurrentDateString() {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		return dateFormat.format(calendar.getTime());
	}

	private String basePath;

	private String baseFilesystemPath;

	private String indexResource;

	private Map<String, String> mimeTypeConfig;

	private HttpServer server;

	public StaticHttpServerHandler(HttpAsyncSocket<HttpResponse> channel, String basePath, String baseFilesystemPath,
			String indexResource, Map<String, String> mimeTypeConfig, HttpServer server) {
		super(channel);
		this.basePath = basePath;
		this.baseFilesystemPath = baseFilesystemPath;
		this.indexResource = indexResource;
		this.mimeTypeConfig = mimeTypeConfig;
		this.server = server;
	}
	
	@Override
	public CompletableFuture<Void> onRequestEnd(HttpRequest request) {
		if ("GET".equals(request.getMethod())) {
			if (request.getResource().startsWith(basePath)) {
				String localResource = request.getParsedResource().getResource().substring(basePath.length());
				if (localResource.isEmpty() && !basePath.equals("/")) {
					HttpResponse response = createSimpleResponse(302, "Found");
					response.setHeader("Location", basePath + "/");
					return server.response(channel, response);
				} else {
					while (localResource.startsWith("/")) {
						localResource = localResource.substring(1);
					}
					if (localResource.isEmpty()) {
						localResource = indexResource;
					}
					if (!localResource.contains("..")) {
						Path path = FileSystems.getDefault().getPath(baseFilesystemPath, localResource);
						if (Files.exists(path)) {
							HttpResponse response = new HttpResponse("HTTP/1.1", 200, "Found");
							response.setHeader("Server", "Scafa");
							response.setHeader("Date", getCurrentDateString());
							String connection = request.getHeader("Connection");
							if (!"close".equals(connection)) {
								connection = "keep-alive";
							}
							response.setHeader("Connection", connection);
							int dotPosition = localResource.lastIndexOf('.');
							if (dotPosition >= 0) {
								String contentType = mimeTypeConfig.get(localResource.substring(dotPosition + 1).toLowerCase());
								if (contentType != null) {
									response.setHeader("Content-Type", contentType);
								}
							}
							return server.response(channel, response, path);
						} else {
							return sendSimpleMessage(404, "The resource " + localResource + " does not exist");
						}
					} else {
						return sendSimpleMessage(400, "No path traversal");
					}
				}
			} else {
				return sendSimpleMessage(404, "Resource " + request.getResource() +  " not found");
			}
		} else {
			return sendSimpleMessage(405, "Only GET allowed");
		}
	}

	private CompletableFuture<Void> sendSimpleMessage(int httpCode, String message) {
		HttpResponse response = createSimpleResponse(httpCode, message);
		return server.response(channel, response);
	}

	private HttpResponse createSimpleResponse(int httpCode, String message) {
		HttpResponse response = new HttpResponse("HTTP/1.1", httpCode, message);
		response.setHeader("Server", "Scafa");
		response.setHeader("Content-Length", "0");
		return response;
	}
}
