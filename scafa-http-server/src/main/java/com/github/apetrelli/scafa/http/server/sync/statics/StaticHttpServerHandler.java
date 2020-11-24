package com.github.apetrelli.scafa.http.server.sync.statics;

import java.nio.ByteBuffer;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.HttpUtils;
import com.github.apetrelli.scafa.http.server.sync.HttpServer;
import com.github.apetrelli.scafa.http.server.sync.impl.HttpServerHandlerSupport;
import com.github.apetrelli.scafa.http.sync.HttpSyncSocket;

public class StaticHttpServerHandler extends HttpServerHandlerSupport {

	private String basePath;

	private String baseFilesystemPath;

	private String indexResource;

	private Map<String, String> mimeTypeConfig;

	private HttpServer server;
	
	private ByteBuffer writeBuffer;

	public StaticHttpServerHandler(HttpSyncSocket<HttpResponse> channel, String basePath, String baseFilesystemPath,
			String indexResource, Map<String, String> mimeTypeConfig, HttpServer server) {
		super(channel);
		this.basePath = basePath;
		this.baseFilesystemPath = baseFilesystemPath;
		this.indexResource = indexResource;
		this.mimeTypeConfig = mimeTypeConfig;
		this.server = server;
		writeBuffer = ByteBuffer.allocate(16384);
	}
	
	@Override
	public void onRequestEnd(HttpRequest request) {
		if ("GET".equals(request.getMethod())) {
			if (request.getResource().startsWith(basePath)) {
				String localResource = request.getParsedResource().getResource().substring(basePath.length());
				if (localResource.isEmpty() && !basePath.equals("/")) {
					HttpResponse response = createSimpleResponse(302, "Found");
					response.setHeader("Location", basePath + "/");
					server.response(channel, response);
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
							response.setHeader("Date", HttpUtils.getCurrentHttpDate());
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
							server.response(channel, response, path, writeBuffer);
						} else {
							sendSimpleMessage(404, "The resource " + localResource + " does not exist");
						}
					} else {
						sendSimpleMessage(400, "No path traversal");
					}
				}
			} else {
				sendSimpleMessage(404, "Resource " + request.getResource() +  " not found");
			}
		} else {
			sendSimpleMessage(405, "Only GET allowed");
		}
	}

	private void sendSimpleMessage(int httpCode, String message) {
		HttpResponse response = createSimpleResponse(httpCode, message);
		server.response(channel, response);
	}

	private HttpResponse createSimpleResponse(int httpCode, String message) {
		HttpResponse response = new HttpResponse("HTTP/1.1", httpCode, message);
		response.setHeader("Server", "Scafa");
		response.setHeader("Content-Length", "0");
		return response;
	}
}
