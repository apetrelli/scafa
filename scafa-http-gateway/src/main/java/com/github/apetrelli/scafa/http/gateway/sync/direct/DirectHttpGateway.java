package com.github.apetrelli.scafa.http.gateway.sync.direct;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.http.HttpProcessingContext;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.gateway.sync.GatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.http.gateway.sync.GatewayHttpConnectionFactoryFactory;
import com.github.apetrelli.scafa.http.gateway.sync.handler.DefaultGatewayHttpHandlerFactory;
import com.github.apetrelli.scafa.http.impl.HttpProcessingContextFactory;
import com.github.apetrelli.scafa.http.impl.HttpStateMachine;
import com.github.apetrelli.scafa.http.sync.HttpHandler;
import com.github.apetrelli.scafa.http.sync.HttpSyncSocket;
import com.github.apetrelli.scafa.http.sync.direct.DirectHttpSyncSocketFactory;
import com.github.apetrelli.scafa.http.sync.impl.SyncHttpSink;
import com.github.apetrelli.scafa.http.sync.output.impl.DefaultDataSenderFactory;
import com.github.apetrelli.scafa.proto.SocketFactory;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.data.Input;
import com.github.apetrelli.scafa.proto.data.impl.SimpleInputFactory;
import com.github.apetrelli.scafa.proto.processor.HandlerFactory;
import com.github.apetrelli.scafa.sync.proto.RunnableStarter;
import com.github.apetrelli.scafa.sync.proto.ScafaListener;
import com.github.apetrelli.scafa.sync.proto.SyncServerSocketFactory;
import com.github.apetrelli.scafa.sync.proto.SyncSocket;
import com.github.apetrelli.scafa.sync.proto.processor.DataHandler;
import com.github.apetrelli.scafa.sync.proto.processor.impl.DefaultProcessorFactory;
import com.github.apetrelli.scafa.sync.proto.processor.impl.PassthroughInputProcessorFactory;
import com.github.apetrelli.scafa.sync.proto.processor.impl.StatefulInputProcessorFactory;

public class DirectHttpGateway {

	private static final Logger LOG = Logger.getLogger(DirectHttpGateway.class.getName());

	private SocketFactory<SyncSocket> clientSocketFactory;

	private SyncServerSocketFactory<SyncSocket> serverSocketFactory;

	private RunnableStarter runnableStarter;

	private HostPort destinationSocketAddress;

	private ScafaListener<HttpHandler, SyncSocket> listener;

	public DirectHttpGateway(SocketFactory<SyncSocket> clientSocketFactory,
			SyncServerSocketFactory<SyncSocket> serverSocketFactory, RunnableStarter runnableStarter,
			HostPort destinationSocketAddress) {
		this.clientSocketFactory = clientSocketFactory;
		this.serverSocketFactory = serverSocketFactory;
		this.runnableStarter = runnableStarter;
		this.destinationSocketAddress = destinationSocketAddress;
	}

	public void launch() {
		HttpStateMachine<HttpHandler, Void> stateMachine = new HttpStateMachine<>(new SyncHttpSink());
		StatefulInputProcessorFactory<HttpHandler, HttpProcessingContext> inputProcessorFactory = new StatefulInputProcessorFactory<>(
				stateMachine);
		HttpProcessingContextFactory processingContextFactory = new HttpProcessingContextFactory();
		SocketFactory<HttpSyncSocket<HttpRequest>> socketFactory = new DirectHttpSyncSocketFactory(clientSocketFactory,
				new DefaultDataSenderFactory());
		DefaultProcessorFactory<Input, DataHandler> clientProcessorFactory = new DefaultProcessorFactory<>(
				new PassthroughInputProcessorFactory(), new SimpleInputFactory());
		GatewayHttpConnectionFactory<HttpSyncSocket<HttpRequest>> connectionFactory = new DirectGatewayHttpConnectionFactory(
				socketFactory, clientProcessorFactory, runnableStarter, destinationSocketAddress);
		GatewayHttpConnectionFactoryFactory<HttpSyncSocket<HttpRequest>> factoryFactory = new DefaultGatewayHttpConnectionFactoryFactory<>(
				connectionFactory);
		HandlerFactory<HttpHandler, SyncSocket> handlerFactory = new DefaultGatewayHttpHandlerFactory<>(factoryFactory);
		DefaultProcessorFactory<HttpProcessingContext, HttpHandler> defaultProcessorFactory = new DefaultProcessorFactory<>(
				inputProcessorFactory, processingContextFactory);
		listener = new ScafaListener<>(serverSocketFactory, defaultProcessorFactory, handlerFactory, runnableStarter);
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
