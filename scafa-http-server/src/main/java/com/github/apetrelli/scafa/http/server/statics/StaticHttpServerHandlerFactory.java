package com.github.apetrelli.scafa.http.server.statics;

import java.util.Map;

import com.github.apetrelli.scafa.http.server.HttpServer;
import com.github.apetrelli.scafa.http.server.HttpServerHandler;
import com.github.apetrelli.scafa.http.server.HttpServerHandlerFactory;
import com.github.apetrelli.scafa.proto.aio.AsyncSocket;

public class StaticHttpServerHandlerFactory implements HttpServerHandlerFactory {

	private String basePath;

	private String baseFilesystemPath;

	private String indexResource;

	private Map<String, String> mimeTypeConfig;

	private HttpServer server;

	public StaticHttpServerHandlerFactory(String basePath, String baseFilesystemPath, String indexResource,
			Map<String, String> mimeTypeConfig, HttpServer server) {
		this.basePath = basePath;
		this.baseFilesystemPath = baseFilesystemPath;
		this.indexResource = indexResource;
		this.mimeTypeConfig = mimeTypeConfig;
		this.server = server;
	}

	@Override
	public HttpServerHandler create(AsyncSocket channel) {
		return new StaticHttpServerHandler(channel, basePath, baseFilesystemPath, indexResource, mimeTypeConfig,
				server);
	}

}
