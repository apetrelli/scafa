package com.github.apetrelli.scafa.sync.http.gateway.direct;

import java.io.IOException;
import java.util.logging.Level;

import com.github.apetrelli.scafa.http.HttpProcessingContext;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.impl.HttpProcessingContextFactory;
import com.github.apetrelli.scafa.http.impl.HttpStateMachine;
import com.github.apetrelli.scafa.proto.SocketFactory;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.data.Input;
import com.github.apetrelli.scafa.proto.data.impl.SimpleInputFactory;
import com.github.apetrelli.scafa.proto.processor.HandlerFactory;
import com.github.apetrelli.scafa.sync.http.HttpHandler;
import com.github.apetrelli.scafa.sync.http.HttpSyncSocket;
import com.github.apetrelli.scafa.sync.http.gateway.GatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.sync.http.gateway.GatewayHttpConnectionFactoryFactory;
import com.github.apetrelli.scafa.sync.http.gateway.handler.DefaultGatewayHttpHandlerFactory;
import com.github.apetrelli.scafa.sync.http.impl.SyncHttpSink;
import com.github.apetrelli.scafa.sync.http.output.impl.DefaultDataSenderFactory;
import com.github.apetrelli.scafa.sync.http.socket.direct.DirectHttpSyncSocketFactory;
import com.github.apetrelli.scafa.sync.proto.RunnableStarter;
import com.github.apetrelli.scafa.sync.proto.ScafaListener;
import com.github.apetrelli.scafa.sync.proto.SyncServerSocketFactory;
import com.github.apetrelli.scafa.sync.proto.SyncSocket;
import com.github.apetrelli.scafa.sync.proto.processor.DataHandler;
import com.github.apetrelli.scafa.sync.proto.processor.impl.DefaultProcessorFactory;
import com.github.apetrelli.scafa.sync.proto.processor.impl.PassthroughInputProcessorFactory;
import com.github.apetrelli.scafa.sync.proto.processor.impl.StatefulInputProcessorFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@RequiredArgsConstructor
@Log
public class DirectHttpGateway {

	private final SocketFactory<SyncSocket> clientSocketFactory;

	private final SyncServerSocketFactory<SyncSocket> serverSocketFactory;

	private final RunnableStarter runnableStarter;

	private final HostPort destinationSocketAddress;

	private ScafaListener<HttpHandler, SyncSocket> listener;

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
			log.log(Level.SEVERE, "Cannot start listener", e);
		}

	}

	public void stop() {
		if (listener != null) {
			listener.stop();
		}
	}
}
