package com.github.apetrelli.scafa.http.gateway.direct;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.http.HttpHandler;
import com.github.apetrelli.scafa.http.HttpProcessingContext;
import com.github.apetrelli.scafa.http.gateway.GatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.http.gateway.GatewayHttpConnectionFactoryFactory;
import com.github.apetrelli.scafa.http.gateway.impl.DefaultGatewayHttpHandlerFactory;
import com.github.apetrelli.scafa.http.impl.AsyncHttpSink;
import com.github.apetrelli.scafa.http.impl.HttpProcessingContextFactory;
import com.github.apetrelli.scafa.http.impl.HttpStateMachine;
import com.github.apetrelli.scafa.http.output.DataSenderFactory;
import com.github.apetrelli.scafa.http.output.impl.DefaultDataSenderFactory;
import com.github.apetrelli.scafa.proto.aio.AsyncServerSocketFactory;
import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.aio.HandlerFactory;
import com.github.apetrelli.scafa.proto.aio.ScafaListener;
import com.github.apetrelli.scafa.proto.aio.SocketFactory;
import com.github.apetrelli.scafa.proto.aio.impl.DirectAsyncServerSocketFactory;
import com.github.apetrelli.scafa.proto.aio.impl.DirectClientAsyncSocketFactory;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.processor.impl.DefaultProcessorFactory;
import com.github.apetrelli.scafa.proto.processor.impl.StatefulInputProcessorFactory;

public class DirectHttpGateway {

	private static final Logger LOG = Logger.getLogger(DirectHttpGateway.class.getName());

	private int port;

	private String interfaceName;

	private boolean forceIpV4;

	private HostPort destinationSocketAddress;

	private ScafaListener<HttpHandler, AsyncSocket> listener;

	public DirectHttpGateway(int port, String interfaceName, boolean forceIpV4, HostPort destinationSocketAddress) {
		this.port = port;
		this.interfaceName = interfaceName;
		this.forceIpV4 = forceIpV4;
		this.destinationSocketAddress = destinationSocketAddress;
	}

	public void launch() {
    	HttpStateMachine<HttpHandler, CompletableFuture<Void>> stateMachine = new HttpStateMachine<>(new AsyncHttpSink());
        StatefulInputProcessorFactory<HttpHandler, HttpProcessingContext> inputProcessorFactory = new StatefulInputProcessorFactory<>(stateMachine);
        HttpProcessingContextFactory processingContextFactory = new HttpProcessingContextFactory();
        SocketFactory<AsyncSocket> socketFactory = new DirectClientAsyncSocketFactory();
        DataSenderFactory dataSenderFactory = new DefaultDataSenderFactory();
		GatewayHttpConnectionFactory connectionFactory = new DirectGatewayHttpConnectionFactory(socketFactory,
				dataSenderFactory, destinationSocketAddress);
        GatewayHttpConnectionFactoryFactory factoryFactory = new DirectHttpConnectionFactoryFactory(connectionFactory);
        HandlerFactory<HttpHandler, AsyncSocket> handlerFactory = new DefaultGatewayHttpHandlerFactory(factoryFactory);
        AsyncServerSocketFactory<AsyncSocket> asyncServerSocketFactory = new DirectAsyncServerSocketFactory(port, interfaceName, forceIpV4);
        DefaultProcessorFactory<HttpProcessingContext, HttpHandler> defaultProcessorFactory = new DefaultProcessorFactory<>(
                inputProcessorFactory, processingContextFactory);
		listener = new ScafaListener<>(asyncServerSocketFactory, defaultProcessorFactory,
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
