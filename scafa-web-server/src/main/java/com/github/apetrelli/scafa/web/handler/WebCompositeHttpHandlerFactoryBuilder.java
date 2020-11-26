package com.github.apetrelli.scafa.web.handler;

import java.util.Map;

import com.github.apetrelli.scafa.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.http.HttpHandler;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.gateway.GatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.http.gateway.GatewayHttpConnectionFactoryFactory;
import com.github.apetrelli.scafa.http.gateway.direct.DirectGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.http.gateway.direct.DirectHttpConnectionFactoryFactory;
import com.github.apetrelli.scafa.http.gateway.impl.DefaultGatewayHttpHandlerFactory;
import com.github.apetrelli.scafa.http.impl.CompositeHttpHandlerFactory;
import com.github.apetrelli.scafa.http.impl.CompositeHttpHandlerFactory.CompositeHttpHandlerFactoryBuilder;
import com.github.apetrelli.scafa.http.output.DataSenderFactory;
import com.github.apetrelli.scafa.http.server.HttpServer;
import com.github.apetrelli.scafa.http.server.impl.HttpServerHandlerAdapterFactory;
import com.github.apetrelli.scafa.http.server.statics.StaticHttpServerHandlerFactory;
import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.aio.HandlerFactory;
import com.github.apetrelli.scafa.proto.aio.SocketFactory;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.processor.DataHandler;
import com.github.apetrelli.scafa.proto.processor.ProcessorFactory;

public class WebCompositeHttpHandlerFactoryBuilder {
	
	public class StaticHttpServerHandlerFactoryBuilder {
		private String basePath;
		
		private String basePathPattern;
		
		private String baseFilesystemPath;
		
		private String indexResource = "index.html";

		private Map<String, String> mimeTypeConfig;
		
		private StaticHttpServerHandlerFactoryBuilder(Map<String, String> mimeTypeConfig) {
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
		
		public StaticHttpServerHandlerFactoryBuilder withMimeTypeConfig(Map<String, String> mimeTypeConfig) {
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
			GatewayHttpConnectionFactory<HttpAsyncSocket<HttpRequest>> connectionFactory = new DirectGatewayHttpConnectionFactory(socketFactory,
					dataSenderFactory, clientProcessorFactory, destinationSocketAddress);
	        GatewayHttpConnectionFactoryFactory<HttpAsyncSocket<HttpRequest>> factoryFactory = new DirectHttpConnectionFactoryFactory(connectionFactory);
	        HandlerFactory<HttpHandler, AsyncSocket> handlerFactory = new DefaultGatewayHttpHandlerFactory<>(factoryFactory);
	        WebCompositeHttpHandlerFactoryBuilder.this.innerBuilder.withPattern(basePathPattern, handlerFactory);
			return WebCompositeHttpHandlerFactoryBuilder.this;
		}
	}

	private CompositeHttpHandlerFactoryBuilder innerBuilder = new CompositeHttpHandlerFactoryBuilder();
	
	private HttpServer httpServer;
	
    private SocketFactory<AsyncSocket> socketFactory;
    
    private DataSenderFactory dataSenderFactory;
	
    private ProcessorFactory<DataHandler, AsyncSocket> clientProcessorFactory;

	private Map<String, String> mimeTypeConfig;
	
	public CompositeHttpHandlerFactory build() {
		return innerBuilder.build();
	}
	
	public WebCompositeHttpHandlerFactoryBuilder withHttpServer(HttpServer httpServer) {
		this.httpServer = httpServer;
		return this;
	}
	
	public WebCompositeHttpHandlerFactoryBuilder withSocketFactory(SocketFactory<AsyncSocket> socketFactory) {
		this.socketFactory = socketFactory;
		return this;
	}
	
	public WebCompositeHttpHandlerFactoryBuilder withDataSenderFactory(DataSenderFactory dataSenderFactory) {
		this.dataSenderFactory = dataSenderFactory;
		return this;
	}
	
	public WebCompositeHttpHandlerFactoryBuilder withClientProcessorFactory(ProcessorFactory<DataHandler, AsyncSocket> clientProcessorFactory) {
		this.clientProcessorFactory = clientProcessorFactory;
		return this;
	}
	
	public WebCompositeHttpHandlerFactoryBuilder withMimeTypeConfig(Map<String, String> mimeTypeConfig) {
		this.mimeTypeConfig = mimeTypeConfig;
		return this;
	}
	
	public WebCompositeHttpHandlerFactoryBuilder withDefaultHandlerFactory(
			HandlerFactory<HttpHandler, HttpAsyncSocket<HttpResponse>> defaultHandlerFactory) {
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
