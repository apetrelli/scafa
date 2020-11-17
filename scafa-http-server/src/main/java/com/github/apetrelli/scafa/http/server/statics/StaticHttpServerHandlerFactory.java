package com.github.apetrelli.scafa.http.server.statics;

import java.util.Map;
import java.util.TimeZone;

import com.github.apetrelli.scafa.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.server.HttpServer;
import com.github.apetrelli.scafa.http.server.HttpServerHandler;
import com.github.apetrelli.scafa.http.server.HttpServerHandlerFactory;

public class StaticHttpServerHandlerFactory implements HttpServerHandlerFactory {

	private String basePath;

	private String baseFilesystemPath;

	private String indexResource;

	private Map<String, String> mimeTypeConfig;

	private HttpServer server;
	
	private TimeZone timeZone;

	public StaticHttpServerHandlerFactory(String basePath, String baseFilesystemPath, String indexResource,
			Map<String, String> mimeTypeConfig, HttpServer server, TimeZone timeZone) {
		this.basePath = basePath;
		this.baseFilesystemPath = baseFilesystemPath;
		this.indexResource = indexResource;
		this.mimeTypeConfig = mimeTypeConfig;
		this.server = server;
		this.timeZone = timeZone;
	}

	@Override
	public HttpServerHandler create(HttpAsyncSocket<HttpResponse> channel) {
		return new StaticHttpServerHandler(channel, basePath, baseFilesystemPath, indexResource, mimeTypeConfig,
				server, timeZone);
	}

}
