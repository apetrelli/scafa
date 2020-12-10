package com.github.apetrelli.scafa.http.server.statics;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import com.github.apetrelli.scafa.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.server.HttpServer;
import com.github.apetrelli.scafa.http.server.HttpServerHandler;
import com.github.apetrelli.scafa.http.server.HttpServerHandlerFactory;
import com.github.apetrelli.scafa.proto.util.AsciiString;

public class StaticHttpServerHandlerFactory implements HttpServerHandlerFactory {

	private String basePath;

	private String indexResource;

	private Map<String, AsciiString> mimeTypeConfig;

	private HttpServer server;
	
	private Map<String, Path> localResource2path;

	public StaticHttpServerHandlerFactory(String basePath, String baseFilesystemPath, String indexResource,
			Map<String, AsciiString> mimeTypeConfig, HttpServer server) {
		this.basePath = basePath;
		this.indexResource = indexResource;
		this.mimeTypeConfig = mimeTypeConfig;
		this.server = server;
		localResource2path = new HashMap<>();
		scavengeDirectory(FileSystems.getDefault().getPath(baseFilesystemPath), "");
	}

	@Override
	public HttpServerHandler create(HttpAsyncSocket<HttpResponse> channel) {
		return new StaticHttpServerHandler(channel, basePath, localResource2path, indexResource, mimeTypeConfig,
				server);
	}

	private void scavengeDirectory(Path path, String prefix) {
		try (Stream<Path> stream = Files.list(path)) {
			stream.forEach(x -> {
				if (Files.isDirectory(x)) {
					scavengeDirectory(x, prefix + x.getFileName() + "/");
				} else {
					localResource2path.put(prefix + x.getFileName(), x);
				}
			});
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

}
