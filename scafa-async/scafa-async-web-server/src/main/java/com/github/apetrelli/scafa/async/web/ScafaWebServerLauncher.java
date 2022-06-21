package com.github.apetrelli.scafa.async.web;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.github.apetrelli.scafa.async.file.PathBufferContextReaderFactory;
import com.github.apetrelli.scafa.async.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.async.http.HttpHandler;
import com.github.apetrelli.scafa.async.http.impl.AsyncHttpSink;
import com.github.apetrelli.scafa.async.http.output.DataSenderFactory;
import com.github.apetrelli.scafa.async.http.output.impl.DefaultDataSenderFactory;
import com.github.apetrelli.scafa.async.http.server.HttpServer;
import com.github.apetrelli.scafa.async.http.server.impl.DefaultHttpServer;
import com.github.apetrelli.scafa.async.http.server.impl.HttpServerHandlerAdapterFactory;
import com.github.apetrelli.scafa.async.http.server.statics.NotFoundHttpServerHandlerFactory;
import com.github.apetrelli.scafa.async.http.socket.direct.DirectHttpAsyncSocketFactory;
import com.github.apetrelli.scafa.async.http.socket.server.HttpAsyncServerSocketFactory;
import com.github.apetrelli.scafa.async.proto.ScafaListener;
import com.github.apetrelli.scafa.async.proto.processor.DataHandler;
import com.github.apetrelli.scafa.async.proto.processor.impl.DefaultProcessorFactory;
import com.github.apetrelli.scafa.async.proto.processor.impl.PassthroughInputProcessorFactory;
import com.github.apetrelli.scafa.async.proto.processor.impl.StatefulInputProcessorFactory;
import com.github.apetrelli.scafa.async.proto.socket.AsyncServerSocketFactory;
import com.github.apetrelli.scafa.async.proto.socket.AsyncServerSocketFactoryFactory;
import com.github.apetrelli.scafa.async.proto.socket.AsyncSocket;
import com.github.apetrelli.scafa.http.HttpProcessingContext;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.impl.HttpProcessingContextFactory;
import com.github.apetrelli.scafa.http.impl.HttpStateMachine;
import com.github.apetrelli.scafa.proto.SocketFactory;
import com.github.apetrelli.scafa.proto.data.impl.SimpleInputFactory;
import com.github.apetrelli.scafa.proto.processor.HandlerFactory;
import com.github.apetrelli.scafa.proto.processor.ProcessorFactory;
import com.github.apetrelli.scafa.proto.util.AsciiString;
import com.github.apetrelli.scafa.web.AbstractScafaWebServerLauncher;
import com.github.apetrelli.scafa.web.config.Configuration;
import com.github.apetrelli.scafa.web.config.PathConfiguration;
import com.github.apetrelli.scafa.web.config.SocketConfiguration;
import com.github.apetrelli.scafa.web.config.StaticPathConfiguration;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Log
@RequiredArgsConstructor
public class ScafaWebServerLauncher extends AbstractScafaWebServerLauncher {
	
	private final SocketFactory<AsyncSocket> clientSocketFactory;
	
	private final AsyncServerSocketFactoryFactory serverSocketFactoryFactory;
	
	private final PathBufferContextReaderFactory pathBufferContextReaderFactory;

	private List<ScafaListener<HttpHandler, HttpAsyncSocket<HttpResponse>>> listeners;
	
	@Override
	public void stop() {
		listeners.forEach(ScafaListener::stop);
	}

	@Override
	protected void launch(Configuration config) {
		HttpStateMachine<HttpHandler, CompletableFuture<Void>> stateMachine = new HttpStateMachine<>(new AsyncHttpSink());
		StatefulInputProcessorFactory<HttpHandler, HttpProcessingContext> inputProcessorFactory = new StatefulInputProcessorFactory<>(stateMachine);
		HttpProcessingContextFactory processingContextFactory = new HttpProcessingContextFactory();
		DataSenderFactory dataSenderFactory = new DefaultDataSenderFactory();
		SocketFactory<HttpAsyncSocket<HttpRequest>> socketFactory = new DirectHttpAsyncSocketFactory(clientSocketFactory, dataSenderFactory);
		DefaultProcessorFactory<HttpProcessingContext, HttpHandler> defaultProcessorFactory = new DefaultProcessorFactory<>(
		        inputProcessorFactory, processingContextFactory);
		HttpServer server = new DefaultHttpServer(dataSenderFactory, pathBufferContextReaderFactory);
		NotFoundHttpServerHandlerFactory notFoundFactory = new NotFoundHttpServerHandlerFactory(server);
		HandlerFactory<HttpHandler, HttpAsyncSocket<HttpResponse>> defaultHandlerFactory = new HttpServerHandlerAdapterFactory(notFoundFactory);
		ProcessorFactory<DataHandler, AsyncSocket> clientProcessorFactory = new DefaultProcessorFactory<>(
				new PassthroughInputProcessorFactory(), new SimpleInputFactory());
		listeners = config.getSocketConfigurations().stream()
				.map(x -> createListener(config.getMimeTypeConfig(), x, dataSenderFactory, socketFactory,
						defaultProcessorFactory, clientProcessorFactory, server, defaultHandlerFactory))
				.collect(Collectors.toList());
		listeners.forEach(x -> {
			try {
				x.listen();
			} catch (IOException e) {
		        log.log(Level.SEVERE, "Cannot start listener", e);
			}
		});
	}

	private ScafaListener<HttpHandler, HttpAsyncSocket<HttpResponse>> createListener(Map<String, AsciiString> mimeTypeConfig,
			SocketConfiguration socketConfig, DataSenderFactory dataSenderFactory,
			SocketFactory<HttpAsyncSocket<HttpRequest>> socketFactory,
			DefaultProcessorFactory<HttpProcessingContext, HttpHandler> defaultProcessorFactory,
			ProcessorFactory<DataHandler, AsyncSocket> clientProcessorFactory, HttpServer server,
			HandlerFactory<HttpHandler, HttpAsyncSocket<HttpResponse>> defaultHandlerFactory) {
		AsyncServerSocketFactory<AsyncSocket> serverSocketFactory = serverSocketFactoryFactory.create(socketConfig.getPort());
		WebCompositeHttpHandlerFactoryBuilder builder = new WebCompositeHttpHandlerFactoryBuilder();
		builder.withMimeTypeConfig(mimeTypeConfig)
				.withSocketFactory(socketFactory).withHttpServer(server)
				.withDefaultHandlerFactory(defaultHandlerFactory).withClientProcessorFactory(clientProcessorFactory);
        socketConfig.getPaths().forEach(x -> buildPortion(x, builder));
        AsyncServerSocketFactory<HttpAsyncSocket<HttpResponse>> httpServerSocketFactory = new HttpAsyncServerSocketFactory<>(serverSocketFactory, dataSenderFactory);
		return new ScafaListener<>(httpServerSocketFactory, defaultProcessorFactory, builder.build());
	}

	private void buildPortion(PathConfiguration x, WebCompositeHttpHandlerFactoryBuilder builder) {
		if (x instanceof StaticPathConfiguration) {
			StaticPathConfiguration staticPath = (StaticPathConfiguration) x;
			builder.withStaticServer().withBaseFilesystemPath(staticPath.getBaseFilesystemPath())
					.withBasePath(staticPath.getBasePath()).withIndexResource(staticPath.getIndexResource()).and();
		}
	}
}
