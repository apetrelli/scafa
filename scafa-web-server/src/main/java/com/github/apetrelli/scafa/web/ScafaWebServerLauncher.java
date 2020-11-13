package com.github.apetrelli.scafa.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.ini4j.Ini;

import com.github.apetrelli.scafa.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.http.HttpHandler;
import com.github.apetrelli.scafa.http.HttpProcessingContext;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.impl.AsyncHttpSink;
import com.github.apetrelli.scafa.http.impl.HttpAsyncServerSocketFactory;
import com.github.apetrelli.scafa.http.impl.HttpProcessingContextFactory;
import com.github.apetrelli.scafa.http.impl.HttpStateMachine;
import com.github.apetrelli.scafa.http.output.DataSenderFactory;
import com.github.apetrelli.scafa.http.output.impl.DefaultDataSenderFactory;
import com.github.apetrelli.scafa.http.server.HttpServer;
import com.github.apetrelli.scafa.http.server.impl.DefaultHttpServer;
import com.github.apetrelli.scafa.http.server.impl.HttpServerHandlerAdapterFactory;
import com.github.apetrelli.scafa.http.server.statics.NotFoundHttpServerHandlerFactory;
import com.github.apetrelli.scafa.proto.aio.AsyncServerSocketFactory;
import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.aio.HandlerFactory;
import com.github.apetrelli.scafa.proto.aio.ScafaListener;
import com.github.apetrelli.scafa.proto.aio.SocketFactory;
import com.github.apetrelli.scafa.proto.aio.impl.DirectAsyncServerSocketFactory;
import com.github.apetrelli.scafa.proto.aio.impl.DirectClientAsyncSocketFactory;
import com.github.apetrelli.scafa.proto.processor.impl.DefaultProcessorFactory;
import com.github.apetrelli.scafa.proto.processor.impl.StatefulInputProcessorFactory;
import com.github.apetrelli.scafa.web.config.PathConfiguration;
import com.github.apetrelli.scafa.web.config.SocketConfiguration;
import com.github.apetrelli.scafa.web.config.StaticPathConfiguration;
import com.github.apetrelli.scafa.web.config.ini.AbstractIniConfiguration;
import com.github.apetrelli.scafa.web.handler.WebCompositeHttpHandlerFactoryBuilder;

public class ScafaWebServerLauncher {
	
	private static final Logger LOG = Logger.getLogger(ScafaWebServerLauncher.class.getName());
	
	private Map<String, String> mimeTypeConfig;
	
	private List<ScafaListener<HttpHandler, HttpAsyncSocket<HttpResponse>>> listeners;
	
	public ScafaWebServerLauncher() throws IOException {
		mimeTypeConfig = new HashMap<>();
		try (InputStream is = getClass().getResourceAsStream("/mime.types");
				InputStreamReader isr = new InputStreamReader(is, StandardCharsets.US_ASCII);
				BufferedReader br = new BufferedReader(isr)) {
			String line;
			while ((line = br.readLine()) != null) {
				if (!line.startsWith("#")) {
					String[] pieces = line.split("\\s+");
					if (pieces.length >= 2) {
						String mimeType = pieces[0];
						for (int i = 1; i < pieces.length; i++) {
							mimeTypeConfig.put(pieces[i], mimeType);
						}
					}
				}
			}
		}
	}
	
	public void launch(String rootFilesystemDirectory) {
		try {
			Ini ini = new Ini(new File(rootFilesystemDirectory, "config.ini"));
			AbstractIniConfiguration config = new AbstractIniConfiguration(ini, rootFilesystemDirectory);
			listeners = config.getSocketConfigurations().stream().map(this::createListener).collect(Collectors.toList());
			listeners.forEach(x -> {
				try {
					x.listen();
				} catch (IOException e) {
		            LOG.log(Level.SEVERE, "Cannot start listener", e);
				}
			});
		} catch (IOException e) {
            LOG.log(Level.SEVERE, "Cannot start web server", e);
		}
	}
	
	public void stop() {
		listeners.forEach(ScafaListener::stop);
	}

	private ScafaListener<HttpHandler,HttpAsyncSocket<HttpResponse>> createListener(SocketConfiguration socketConfig) {
    	HttpStateMachine<HttpHandler, CompletableFuture<Void>> stateMachine = new HttpStateMachine<>(new AsyncHttpSink());
        StatefulInputProcessorFactory<HttpHandler, HttpProcessingContext> inputProcessorFactory = new StatefulInputProcessorFactory<>(stateMachine);
        HttpProcessingContextFactory processingContextFactory = new HttpProcessingContextFactory();
        DataSenderFactory dataSenderFactory = new DefaultDataSenderFactory();
        SocketFactory<AsyncSocket> socketFactory = new DirectClientAsyncSocketFactory();
        DefaultProcessorFactory<HttpProcessingContext, HttpHandler> defaultProcessorFactory = new DefaultProcessorFactory<>(
                inputProcessorFactory, processingContextFactory);
        HttpServer server = new DefaultHttpServer(dataSenderFactory);
        NotFoundHttpServerHandlerFactory notFoundFactory = new NotFoundHttpServerHandlerFactory(server);
        HandlerFactory<HttpHandler, HttpAsyncSocket<HttpResponse>> defaultHandlerFactory = new HttpServerHandlerAdapterFactory(notFoundFactory);
        return createListener(socketConfig, dataSenderFactory, socketFactory, defaultProcessorFactory, server, defaultHandlerFactory);
	}

	private ScafaListener<HttpHandler, HttpAsyncSocket<HttpResponse>> createListener(SocketConfiguration socketConfig,
			DataSenderFactory dataSenderFactory, SocketFactory<AsyncSocket> socketFactory,
			DefaultProcessorFactory<HttpProcessingContext, HttpHandler> defaultProcessorFactory, HttpServer server,
			HandlerFactory<HttpHandler, HttpAsyncSocket<HttpResponse>> defaultHandlerFactory) {
        AsyncServerSocketFactory<AsyncSocket> serverSocketFactory = new DirectAsyncServerSocketFactory(socketConfig.getPort(), null, false);
		WebCompositeHttpHandlerFactoryBuilder builder = new WebCompositeHttpHandlerFactoryBuilder();
		builder.withDataSenderFactory(dataSenderFactory).withMimeTypeConfig(mimeTypeConfig)
				.withSocketFactory(socketFactory).withHttpServer(server)
				.withDefaultHandlerFactory(defaultHandlerFactory);
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
