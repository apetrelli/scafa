package com.github.apetrelli.scafa.http.gateway.direct;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.http.HttpHandler;
import com.github.apetrelli.scafa.http.HttpProcessingContext;
import com.github.apetrelli.scafa.http.HttpStatus;
import com.github.apetrelli.scafa.http.gateway.GatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.http.gateway.GatewayHttpConnectionFactoryFactory;
import com.github.apetrelli.scafa.http.gateway.impl.DefaultGatewayHttpHandlerFactory;
import com.github.apetrelli.scafa.http.impl.HttpProcessingContextFactory;
import com.github.apetrelli.scafa.http.impl.HttpStateMachine;
import com.github.apetrelli.scafa.proto.aio.HandlerFactory;
import com.github.apetrelli.scafa.proto.aio.ScafaListener;
import com.github.apetrelli.scafa.proto.aio.impl.DefaultProcessorFactory;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.processor.impl.StatefulInputProcessorFactory;

public class DirectHttpGateway {

	private static final Logger LOG = Logger.getLogger(DirectHttpGateway.class.getName());

	private int port;

	private String interfaceName;

	private boolean forceIpV4;

	private HostPort destinationSocketAddress;

	private ScafaListener<HttpHandler> listener;

	public DirectHttpGateway(int port, String interfaceName, boolean forceIpV4, HostPort destinationSocketAddress) {
		this.port = port;
		this.interfaceName = interfaceName;
		this.forceIpV4 = forceIpV4;
		this.destinationSocketAddress = destinationSocketAddress;
	}

	public void launch() {
    	HttpStateMachine stateMachine = new HttpStateMachine();
        StatefulInputProcessorFactory<HttpHandler, HttpStatus, HttpProcessingContext> inputProcessorFactory = new StatefulInputProcessorFactory<>(stateMachine);
        HttpProcessingContextFactory processingContextFactory = new HttpProcessingContextFactory();
        GatewayHttpConnectionFactory connectionFactory = new DirectGatewayHttpConnectionFactory(destinationSocketAddress);
        GatewayHttpConnectionFactoryFactory factoryFactory = new DirectHttpConnectionFactoryFactory(connectionFactory);
        HandlerFactory<HttpHandler> handlerFactory = new DefaultGatewayHttpHandlerFactory(factoryFactory);
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
