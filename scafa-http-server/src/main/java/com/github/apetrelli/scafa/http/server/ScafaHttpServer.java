package com.github.apetrelli.scafa.http.server;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.http.HttpHandler;
import com.github.apetrelli.scafa.http.HttpProcessingContext;
import com.github.apetrelli.scafa.http.HttpStatus;
import com.github.apetrelli.scafa.http.impl.HttpProcessingContextFactory;
import com.github.apetrelli.scafa.http.impl.HttpStateMachine;
import com.github.apetrelli.scafa.http.server.impl.HttpServerHandlerAdapterFactory;
import com.github.apetrelli.scafa.proto.aio.ScafaListener;
import com.github.apetrelli.scafa.proto.aio.impl.DefaultProcessorFactory;
import com.github.apetrelli.scafa.proto.processor.impl.StatefulInputProcessorFactory;

public class ScafaHttpServer {

	private static final Logger LOG = Logger.getLogger(ScafaHttpServer.class.getName());

	private HttpServerHandlerFactory factory;

	private int port;

	private String interfaceName;

	private boolean forceIpV4;

	private ScafaListener<HttpHandler> listener;

	public ScafaHttpServer(HttpServerHandlerFactory factory, int port, String interfaceName, boolean forceIpV4) {
		this.factory = factory;
		this.port = port;
		this.interfaceName = interfaceName;
		this.forceIpV4 = forceIpV4;
	}

	public void launch() {
    	HttpStateMachine stateMachine = new HttpStateMachine();
        StatefulInputProcessorFactory<HttpHandler, HttpStatus, HttpProcessingContext> inputProcessorFactory = new StatefulInputProcessorFactory<>(stateMachine);
        HttpProcessingContextFactory processingContextFactory = new HttpProcessingContextFactory();
        HttpServerHandlerAdapterFactory handlerFactory = new HttpServerHandlerAdapterFactory(factory);
        DefaultProcessorFactory<HttpStatus, HttpProcessingContext, HttpHandler> defaultProcessorFactory = new DefaultProcessorFactory<>(
                inputProcessorFactory, processingContextFactory);
        listener = new ScafaListener<HttpHandler>(defaultProcessorFactory, handlerFactory, port, interfaceName, forceIpV4);
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