package com.github.apetrelli.scafa.sync.web;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.github.apetrelli.scafa.http.HttpProcessingContext;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.impl.HttpProcessingContextFactory;
import com.github.apetrelli.scafa.http.impl.HttpStateMachine;
import com.github.apetrelli.scafa.proto.SocketFactory;
import com.github.apetrelli.scafa.proto.data.Input;
import com.github.apetrelli.scafa.proto.data.impl.SimpleInputFactory;
import com.github.apetrelli.scafa.proto.processor.HandlerFactory;
import com.github.apetrelli.scafa.proto.processor.ProcessorFactory;
import com.github.apetrelli.scafa.proto.util.AsciiString;
import com.github.apetrelli.scafa.sync.http.HttpHandler;
import com.github.apetrelli.scafa.sync.http.HttpSyncSocket;
import com.github.apetrelli.scafa.sync.http.impl.SyncHttpSink;
import com.github.apetrelli.scafa.sync.http.output.DataSenderFactory;
import com.github.apetrelli.scafa.sync.http.output.impl.DefaultDataSenderFactory;
import com.github.apetrelli.scafa.sync.http.server.HttpServer;
import com.github.apetrelli.scafa.sync.http.server.impl.DefaultHttpServer;
import com.github.apetrelli.scafa.sync.http.server.impl.HttpServerHandlerAdapterFactory;
import com.github.apetrelli.scafa.sync.http.server.statics.NotFoundHttpServerHandlerFactory;
import com.github.apetrelli.scafa.sync.http.socket.direct.DirectHttpSyncSocketFactory;
import com.github.apetrelli.scafa.sync.http.socket.server.HttpSyncServerSocketFactory;
import com.github.apetrelli.scafa.sync.proto.RunnableStarter;
import com.github.apetrelli.scafa.sync.proto.RunnableStarterFactory;
import com.github.apetrelli.scafa.sync.proto.ScafaListener;
import com.github.apetrelli.scafa.sync.proto.SyncServerSocketFactory;
import com.github.apetrelli.scafa.sync.proto.SyncServerSocketFactoryFactory;
import com.github.apetrelli.scafa.sync.proto.SyncSocket;
import com.github.apetrelli.scafa.sync.proto.processor.DataHandler;
import com.github.apetrelli.scafa.sync.proto.processor.impl.DefaultProcessorFactory;
import com.github.apetrelli.scafa.sync.proto.processor.impl.PassthroughInputProcessorFactory;
import com.github.apetrelli.scafa.sync.proto.processor.impl.StatefulInputProcessorFactory;
import com.github.apetrelli.scafa.web.AbstractScafaWebServerLauncher;
import com.github.apetrelli.scafa.web.config.Configuration;
import com.github.apetrelli.scafa.web.config.PathConfiguration;
import com.github.apetrelli.scafa.web.config.SocketConfiguration;
import com.github.apetrelli.scafa.web.config.StaticPathConfiguration;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@RequiredArgsConstructor
@Log
public class ScafaWebServerLauncher extends AbstractScafaWebServerLauncher {
	
	private final SocketFactory<SyncSocket> clientSocketFactory;
	
	private final SyncServerSocketFactoryFactory serverSocketFactoryFactory;
    
	private final RunnableStarterFactory runnableStarterFactory;
	
	private List<ScafaListener<HttpHandler, HttpSyncSocket<HttpResponse>>> listeners;
	
	@Override
	public void stop() {
		listeners.forEach(ScafaListener::stop);
	}

	@Override
	protected void launch(Configuration config) {
		HttpStateMachine<HttpHandler, Void> stateMachine = new HttpStateMachine<>(new SyncHttpSink());
		try (RunnableStarter runnableStarter = runnableStarterFactory.create();
				DefaultProcessorFactory<HttpProcessingContext, HttpHandler> defaultProcessorFactory = new DefaultProcessorFactory<>(
						new StatefulInputProcessorFactory<>(stateMachine), new HttpProcessingContextFactory());
				DefaultProcessorFactory<Input, DataHandler> clientProcessorFactory = new DefaultProcessorFactory<>(
						new PassthroughInputProcessorFactory(), new SimpleInputFactory())) {
			DataSenderFactory dataSenderFactory = new DefaultDataSenderFactory();
			SocketFactory<HttpSyncSocket<HttpRequest>> socketFactory = new DirectHttpSyncSocketFactory(
					clientSocketFactory, dataSenderFactory);
			HttpServer server = new DefaultHttpServer(dataSenderFactory);
			NotFoundHttpServerHandlerFactory notFoundFactory = new NotFoundHttpServerHandlerFactory(server);
			HandlerFactory<HttpHandler, HttpSyncSocket<HttpResponse>> defaultHandlerFactory = new HttpServerHandlerAdapterFactory(
					notFoundFactory);
			listeners = config.getSocketConfigurations().stream()
					.map(x -> createListener(config.getMimeTypeConfig(), x, dataSenderFactory, socketFactory,
							defaultProcessorFactory, clientProcessorFactory, server, defaultHandlerFactory,
							runnableStarter))
					.collect(Collectors.toList());
			List<Future<?>> futures = listeners.stream().map(x -> runnableStarter.start(() -> {
				try {
					x.listen();
				} catch (IOException e) {
					log.log(Level.SEVERE, "Cannot start listener", e);
				}
			})).collect(Collectors.toList());
			futures.forEach(t -> {
				try {
					t.get();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} catch (ExecutionException e) {
					throw new CompletionException(e);
				}
			});
		}
	}

	private ScafaListener<HttpHandler, HttpSyncSocket<HttpResponse>> createListener(Map<String, AsciiString> mimeTypeConfig,
			SocketConfiguration socketConfig, DataSenderFactory dataSenderFactory,
			SocketFactory<HttpSyncSocket<HttpRequest>> socketFactory,
			DefaultProcessorFactory<HttpProcessingContext, HttpHandler> defaultProcessorFactory,
			ProcessorFactory<DataHandler, SyncSocket> clientProcessorFactory, HttpServer server,
			HandlerFactory<HttpHandler, HttpSyncSocket<HttpResponse>> defaultHandlerFactory,
			RunnableStarter runnableStarter) {
        SyncServerSocketFactory<SyncSocket> serverSocketFactory = serverSocketFactoryFactory.create(socketConfig.getPort());
		WebCompositeHttpHandlerFactoryBuilder builder = new WebCompositeHttpHandlerFactoryBuilder();
		builder.withMimeTypeConfig(mimeTypeConfig)
				.withSocketFactory(socketFactory).withHttpServer(server)
				.withDefaultHandlerFactory(defaultHandlerFactory).withClientProcessorFactory(clientProcessorFactory);
        socketConfig.getPaths().forEach(x -> buildPortion(x, builder));
        SyncServerSocketFactory<HttpSyncSocket<HttpResponse>> httpServerSocketFactory = new HttpSyncServerSocketFactory<>(serverSocketFactory, dataSenderFactory);
		return new ScafaListener<>(httpServerSocketFactory, defaultProcessorFactory, builder.build(), runnableStarter);
	}

	private void buildPortion(PathConfiguration x, WebCompositeHttpHandlerFactoryBuilder builder) {
		if (x instanceof StaticPathConfiguration) {
			StaticPathConfiguration staticPath = (StaticPathConfiguration) x;
			builder.withStaticServer().withBaseFilesystemPath(staticPath.getBaseFilesystemPath())
					.withBasePath(staticPath.getBasePath()).withIndexResource(staticPath.getIndexResource()).and();
		}
	}
}
