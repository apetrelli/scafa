package com.github.apetrelli.scafa.http.server;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import com.github.apetrelli.scafa.http.server.impl.HttpServerHandlerAdapterFactory;
import com.github.apetrelli.scafa.proto.aio.DirectAsyncServerSocketFactory;
import com.github.apetrelli.scafa.proto.async.ScafaListener;
import com.github.apetrelli.scafa.proto.async.processor.impl.DefaultProcessorFactory;
import com.github.apetrelli.scafa.proto.async.processor.impl.StatefulInputProcessorFactory;
import com.github.apetrelli.scafa.proto.async.socket.AsyncServerSocketFactory;
import com.github.apetrelli.scafa.proto.async.socket.AsyncSocket;

public class ScafaHttpServer {

	private static final Logger LOG = Logger.getLogger(ScafaHttpServer.class.getName());

	private HttpServerHandlerFactory factory;

	private int port;

	private String interfaceName;

	private boolean forceIpV4;

	private ScafaListener<HttpHandler, HttpAsyncSocket<HttpResponse>> listener;

	public ScafaHttpServer(HttpServerHandlerFactory factory, int port, String interfaceName, boolean forceIpV4) {
		this.factory = factory;
		this.port = port;
		this.interfaceName = interfaceName;
		this.forceIpV4 = forceIpV4;
	}

	public void launch() {
    	HttpStateMachine<HttpHandler, CompletableFuture<Void>> stateMachine = new HttpStateMachine<>(new AsyncHttpSink());
        StatefulInputProcessorFactory<HttpHandler, HttpProcessingContext> inputProcessorFactory = new StatefulInputProcessorFactory<>(stateMachine);
        HttpProcessingContextFactory processingContextFactory = new HttpProcessingContextFactory();
        HttpServerHandlerAdapterFactory handlerFactory = new HttpServerHandlerAdapterFactory(factory);
        DataSenderFactory dataSenderFactory = new DefaultDataSenderFactory();
        AsyncServerSocketFactory<AsyncSocket> serverSocketFactory = new DirectAsyncServerSocketFactory(port, interfaceName, forceIpV4);
        AsyncServerSocketFactory<HttpAsyncSocket<HttpResponse>> httpServerSocketFactory = new HttpAsyncServerSocketFactory<>(serverSocketFactory, dataSenderFactory);
        DefaultProcessorFactory<HttpProcessingContext, HttpHandler> defaultProcessorFactory = new DefaultProcessorFactory<>(
                inputProcessorFactory, processingContextFactory);
		listener = new ScafaListener<>(httpServerSocketFactory, defaultProcessorFactory,
				handlerFactory);
        try {
			listener.listen();
		} catch (IOException e) {
			LOG.log(Level.SEVERE, "Cannot start listener", e);
		}

	}

	public void stop() {
        if (listener != null) {
            listener.stop();
        }
	}
}
