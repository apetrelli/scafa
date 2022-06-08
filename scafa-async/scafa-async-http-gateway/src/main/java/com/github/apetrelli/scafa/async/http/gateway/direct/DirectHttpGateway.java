package com.github.apetrelli.scafa.async.http.gateway.direct;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.async.proto.ScafaListener;
import com.github.apetrelli.scafa.async.proto.processor.DataHandler;
import com.github.apetrelli.scafa.async.proto.processor.impl.DefaultProcessorFactory;
import com.github.apetrelli.scafa.async.proto.processor.impl.PassthroughInputProcessorFactory;
import com.github.apetrelli.scafa.async.proto.processor.impl.StatefulInputProcessorFactory;
import com.github.apetrelli.scafa.async.proto.socket.AsyncServerSocketFactory;
import com.github.apetrelli.scafa.async.proto.socket.AsyncSocket;
import com.github.apetrelli.scafa.http.HttpProcessingContext;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.async.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.async.http.HttpHandler;
import com.github.apetrelli.scafa.async.http.gateway.GatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.async.http.gateway.GatewayHttpConnectionFactoryFactory;
import com.github.apetrelli.scafa.async.http.gateway.handler.DefaultGatewayHttpHandlerFactory;
import com.github.apetrelli.scafa.async.http.impl.AsyncHttpSink;
import com.github.apetrelli.scafa.async.http.output.impl.DefaultDataSenderFactory;
import com.github.apetrelli.scafa.async.http.socket.direct.DirectHttpAsyncSocketFactory;
import com.github.apetrelli.scafa.http.impl.HttpProcessingContextFactory;
import com.github.apetrelli.scafa.http.impl.HttpStateMachine;
import com.github.apetrelli.scafa.proto.SocketFactory;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.data.Input;
import com.github.apetrelli.scafa.proto.data.impl.SimpleInputFactory;
import com.github.apetrelli.scafa.proto.processor.HandlerFactory;

public class DirectHttpGateway {

	private static final Logger LOG = Logger.getLogger(DirectHttpGateway.class.getName());

	private HostPort destinationSocketAddress;

	private SocketFactory<AsyncSocket> clientSocketFactory;

	private AsyncServerSocketFactory<AsyncSocket> serverSocketFactory;

	private ScafaListener<HttpHandler, AsyncSocket> listener;
	
	public DirectHttpGateway(SocketFactory<AsyncSocket> clientSocketFactory,
			AsyncServerSocketFactory<AsyncSocket> serverSocketFactory, HostPort destinationSocketAddress) {
		this.clientSocketFactory = clientSocketFactory;
		this.serverSocketFactory = serverSocketFactory;
		this.destinationSocketAddress = destinationSocketAddress;
	}

	public void launch() {
    	HttpStateMachine<HttpHandler, CompletableFuture<Void>> stateMachine = new HttpStateMachine<>(new AsyncHttpSink());
        StatefulInputProcessorFactory<HttpHandler, HttpProcessingContext> inputProcessorFactory = new StatefulInputProcessorFactory<>(stateMachine);
        HttpProcessingContextFactory processingContextFactory = new HttpProcessingContextFactory();
		SocketFactory<HttpAsyncSocket<HttpRequest>> socketFactory = new DirectHttpAsyncSocketFactory(
				clientSocketFactory, new DefaultDataSenderFactory());
        DefaultProcessorFactory<Input, DataHandler> clientProcessorFactory = new DefaultProcessorFactory<>(
        		new PassthroughInputProcessorFactory(), new SimpleInputFactory());
		GatewayHttpConnectionFactory<HttpAsyncSocket<HttpRequest>> connectionFactory = new DirectGatewayHttpConnectionFactory(socketFactory,
				clientProcessorFactory, destinationSocketAddress);
        GatewayHttpConnectionFactoryFactory<HttpAsyncSocket<HttpRequest>> factoryFactory = new DefaultGatewayHttpConnectionFactoryFactory<>(connectionFactory);
        HandlerFactory<HttpHandler, AsyncSocket> handlerFactory = new DefaultGatewayHttpHandlerFactory<>(factoryFactory);
        DefaultProcessorFactory<HttpProcessingContext, HttpHandler> defaultProcessorFactory = new DefaultProcessorFactory<>(
                inputProcessorFactory, processingContextFactory);
		listener = new ScafaListener<>(serverSocketFactory, defaultProcessorFactory,
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
