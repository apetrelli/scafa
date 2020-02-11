package com.github.apetrelli.scafa.http.server.statics;

import java.nio.channels.CompletionHandler;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.server.HttpServer;
import com.github.apetrelli.scafa.http.server.impl.HttpServerHandlerSupport;
import com.github.apetrelli.scafa.http.util.HttpUtils;
import com.github.apetrelli.scafa.proto.aio.AsyncSocket;

public class StaticHttpServerHandler extends HttpServerHandlerSupport {

	private String basePath;

	private String baseFilesystemPath;

	private String indexResource;

	private Map<String, String> mimeTypeConfig;

	private HttpServer server;

	public StaticHttpServerHandler(AsyncSocket channel, String basePath, String baseFilesystemPath,
			String indexResource, Map<String, String> mimeTypeConfig, HttpServer server) {
		super(channel);
		this.basePath = basePath;
		this.baseFilesystemPath = baseFilesystemPath;
		this.indexResource = indexResource;
		this.mimeTypeConfig = mimeTypeConfig;
		this.server = server;
	}

	@Override
	public void onRequestEnd(HttpRequest request, CompletionHandler<Void, Void> handler) {
		if ("GET".equals(request.getMethod())) {
			if (request.getResource().startsWith(basePath)) {
				String localResource = request.getParsedResource().getResource().substring(basePath.length());
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
						response.setHeader("Date", HttpUtils.getCurrentDateString());
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
						server.response(channel, response, path, handler);
					} else {
						sendSimpleMessage(404, "The resource " + localResource + " does not exist", handler);
					}
				} else {
					sendSimpleMessage(404, "No path traversal", handler);
				}
			} else {
				sendSimpleMessage(404, "Resource " + request.getResource() +  " not found", handler);
			}
		} else {
			sendSimpleMessage(405, "Only GET allowed", handler);
		}
	}

	private void sendSimpleMessage(int httpCode, String message, CompletionHandler<Void, Void> handler) {
		HttpResponse response = new HttpResponse("HTTP/1.1", httpCode, message);
		response.setHeader("Server", "Scafa");
		response.setHeader("Content-Length", "0");
		server.response(channel, response, handler);
	}
}
