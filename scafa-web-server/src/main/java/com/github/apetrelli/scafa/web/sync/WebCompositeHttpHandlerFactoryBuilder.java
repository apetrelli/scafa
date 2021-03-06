package com.github.apetrelli.scafa.web.sync;

import java.util.Map;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.gateway.sync.GatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.http.gateway.sync.GatewayHttpConnectionFactoryFactory;
import com.github.apetrelli.scafa.http.gateway.sync.direct.DefaultGatewayHttpConnectionFactoryFactory;
import com.github.apetrelli.scafa.http.gateway.sync.direct.DirectGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.http.gateway.sync.handler.DefaultGatewayHttpHandlerFactory;
import com.github.apetrelli.scafa.http.server.sync.HttpServer;
import com.github.apetrelli.scafa.http.server.sync.impl.HttpServerHandlerAdapterFactory;
import com.github.apetrelli.scafa.http.server.sync.statics.StaticHttpServerHandlerFactory;
import com.github.apetrelli.scafa.http.sync.HttpHandler;
import com.github.apetrelli.scafa.http.sync.HttpSyncSocket;
import com.github.apetrelli.scafa.http.sync.composite.CompositeHttpHandlerFactory;
import com.github.apetrelli.scafa.http.sync.composite.CompositeHttpHandlerFactory.CompositeHttpHandlerFactoryBuilder;
import com.github.apetrelli.scafa.proto.aio.HandlerFactory;
import com.github.apetrelli.scafa.proto.aio.SocketFactory;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.processor.ProcessorFactory;
import com.github.apetrelli.scafa.proto.sync.RunnableStarter;
import com.github.apetrelli.scafa.proto.sync.SyncSocket;
import com.github.apetrelli.scafa.proto.sync.processor.DataHandler;
import com.github.apetrelli.scafa.proto.util.AsciiString;

public class WebCompositeHttpHandlerFactoryBuilder {
	
	public class StaticHttpServerHandlerFactoryBuilder {
		private String basePath;
		
		private String basePathPattern;
		
		private String baseFilesystemPath;
		
		private String indexResource = "index.html";

		private Map<String, AsciiString> mimeTypeConfig;
		
		private StaticHttpServerHandlerFactoryBuilder(Map<String, AsciiString> mimeTypeConfig) {
			this.mimeTypeConfig = mimeTypeConfig;
		}

		public StaticHttpServerHandlerFactoryBuilder withBasePath(String basePath) {
			basePath = cleanBasePath(basePath);
			this.basePath = "/" + basePath; // NOSONAR
			this.basePathPattern = "^\\/\\Q" + basePath + "\\E\\/?(\\/.+)?";
			return this;
		}
		
		public StaticHttpServerHandlerFactoryBuilder withBaseFilesystemPath(String baseFilesystemPath) {
			this.baseFilesystemPath = baseFilesystemPath;
			return this;
		}
		
		public StaticHttpServerHandlerFactoryBuilder withIndexResource(String indexResource) {
			this.indexResource = indexResource;
			return this;
		}
		
		public StaticHttpServerHandlerFactoryBuilder withMimeTypeConfig(Map<String, AsciiString> mimeTypeConfig) {
			this.mimeTypeConfig = mimeTypeConfig;
			return this;
		}
		
		public WebCompositeHttpHandlerFactoryBuilder and() {
			WebCompositeHttpHandlerFactoryBuilder.this.innerBuilder.withPattern(basePathPattern,
					new HttpServerHandlerAdapterFactory(new StaticHttpServerHandlerFactory(basePath, baseFilesystemPath,
							indexResource, mimeTypeConfig, httpServer)));
			return WebCompositeHttpHandlerFactoryBuilder.this;
		}
		
		public CompositeHttpHandlerFactory build() {
			return this.and().build();
		}
	}
	
	public class GatewayHttpHandlerFactoryBuilder {
		private String basePathPattern;

		private HostPort destinationSocketAddress;
		
		private GatewayHttpHandlerFactoryBuilder() {
		}

		public GatewayHttpHandlerFactoryBuilder withBasePath(String basePath) {
			this.basePathPattern = createPathPattern(basePath);
			return this;
		}
		
		public GatewayHttpHandlerFactoryBuilder withDestinationSocketAddress(HostPort destinationSocketAddress) {
			this.destinationSocketAddress = destinationSocketAddress;
			return this;
		}
		
		public WebCompositeHttpHandlerFactoryBuilder and() {
			GatewayHttpConnectionFactory<HttpSyncSocket<HttpRequest>> connectionFactory = new DirectGatewayHttpConnectionFactory(socketFactory,
					clientProcessorFactory, runnableStarter, destinationSocketAddress);
	        GatewayHttpConnectionFactoryFactory<HttpSyncSocket<HttpRequest>> factoryFactory = new DefaultGatewayHttpConnectionFactoryFactory<>(connectionFactory);
	        HandlerFactory<HttpHandler, SyncSocket> handlerFactory = new DefaultGatewayHttpHandlerFactory<>(factoryFactory);
	        WebCompositeHttpHandlerFactoryBuilder.this.innerBuilder.withPattern(basePathPattern, handlerFactory);
			return WebCompositeHttpHandlerFactoryBuilder.this;
		}
	}

	private CompositeHttpHandlerFactoryBuilder innerBuilder = new CompositeHttpHandlerFactoryBuilder();
	
	private HttpServer httpServer;
	
    private SocketFactory<HttpSyncSocket<HttpRequest>> socketFactory;
	
    private ProcessorFactory<DataHandler, SyncSocket> clientProcessorFactory;
    
    private RunnableStarter runnableStarter;

	private Map<String, AsciiString> mimeTypeConfig;
	
	public CompositeHttpHandlerFactory build() {
		return innerBuilder.build();
	}
	
	public WebCompositeHttpHandlerFactoryBuilder withHttpServer(HttpServer httpServer) {
		this.httpServer = httpServer;
		return this;
	}
	
	public WebCompositeHttpHandlerFactoryBuilder withSocketFactory(SocketFactory<HttpSyncSocket<HttpRequest>> socketFactory) {
		this.socketFactory = socketFactory;
		return this;
	}
	
	public WebCompositeHttpHandlerFactoryBuilder withClientProcessorFactory(ProcessorFactory<DataHandler, SyncSocket> clientProcessorFactory) {
		this.clientProcessorFactory = clientProcessorFactory;
		return this;
	}
	
	public WebCompositeHttpHandlerFactoryBuilder withRunnableStarter(RunnableStarter runnableStarter) {
		this.runnableStarter = runnableStarter;
		return this;
	}
	
	public WebCompositeHttpHandlerFactoryBuilder withMimeTypeConfig(Map<String, AsciiString> mimeTypeConfig) {
		this.mimeTypeConfig = mimeTypeConfig;
		return this;
	}
	
	public WebCompositeHttpHandlerFactoryBuilder withDefaultHandlerFactory(
			HandlerFactory<HttpHandler, HttpSyncSocket<HttpResponse>> defaultHandlerFactory) {
		innerBuilder.withDefaultHandlerFactory(defaultHandlerFactory);
		return this;
	}
	
	public StaticHttpServerHandlerFactoryBuilder withStaticServer() {
		return new StaticHttpServerHandlerFactoryBuilder(mimeTypeConfig);
	}
	
	public GatewayHttpHandlerFactoryBuilder withGateway() {
		return new GatewayHttpHandlerFactoryBuilder();
	}

	private static String createPathPattern(String basePath) {
		return "^\\/\\Q" + cleanBasePath(basePath) + "\\E\\/?(\\/.+)?";
	}

	private static String cleanBasePath(String basePath) {
		// Cleanup starting and trailing slashes.
		while (basePath.startsWith("/")) {
			basePath = basePath.substring(1);
		}
		while (basePath.endsWith("/")) {
			basePath = basePath.substring(0, basePath.length() - 1);
		}
		return basePath;
	}
}
