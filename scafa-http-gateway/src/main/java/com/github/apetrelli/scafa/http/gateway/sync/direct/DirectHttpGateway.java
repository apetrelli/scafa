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
import com.github.apetrelli.scafa.proto.aio.HandlerFactory;
import com.github.apetrelli.scafa.proto.aio.SocketFactory;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.processor.Input;
import com.github.apetrelli.scafa.proto.processor.impl.SimpleInputFactory;
import com.github.apetrelli.scafa.proto.sync.ScafaListener;
import com.github.apetrelli.scafa.proto.sync.SyncServerSocketFactory;
import com.github.apetrelli.scafa.proto.sync.SyncSocket;
import com.github.apetrelli.scafa.proto.sync.VirtualThreadRunnableStarter;
import com.github.apetrelli.scafa.proto.sync.processor.DataHandler;
import com.github.apetrelli.scafa.proto.sync.processor.impl.DefaultProcessorFactory;
import com.github.apetrelli.scafa.proto.sync.processor.impl.PassthroughInputProcessorFactory;
import com.github.apetrelli.scafa.proto.sync.processor.impl.StatefulInputProcessorFactory;
import com.github.apetrelli.scafa.proto.sync.socket.DirectClientSyncSocketFactory;
import com.github.apetrelli.scafa.proto.sync.socket.DirectSyncServerSocketFactory;

public class DirectHttpGateway {

	private static final Logger LOG = Logger.getLogger(DirectHttpGateway.class.getName());

	private int port;

	private String interfaceName;

	private boolean forceIpV4;

	private HostPort destinationSocketAddress;

	private ScafaListener<HttpHandler, SyncSocket> listener;

	public DirectHttpGateway(int port, String interfaceName, boolean forceIpV4, HostPort destinationSocketAddress) {
		this.port = port;
		this.interfaceName = interfaceName;
		this.forceIpV4 = forceIpV4;
		this.destinationSocketAddress = destinationSocketAddress;
	}

	public void launch() {
    	HttpStateMachine<HttpHandler, Void> stateMachine = new HttpStateMachine<>(new SyncHttpSink());
        StatefulInputProcessorFactory<HttpHandler, HttpProcessingContext> inputProcessorFactory = new StatefulInputProcessorFactory<>(stateMachine);
        HttpProcessingContextFactory processingContextFactory = new HttpProcessingContextFactory();
		SocketFactory<HttpSyncSocket<HttpRequest>> socketFactory = new DirectHttpSyncSocketFactory(
				new DirectClientSyncSocketFactory(), new DefaultDataSenderFactory());
        DefaultProcessorFactory<Input, DataHandler> clientProcessorFactory = new DefaultProcessorFactory<>(
        		new PassthroughInputProcessorFactory(), new SimpleInputFactory());
		VirtualThreadRunnableStarter runnableStarter = new VirtualThreadRunnableStarter();
		GatewayHttpConnectionFactory<HttpSyncSocket<HttpRequest>> connectionFactory = new DirectGatewayHttpConnectionFactory(socketFactory,
				clientProcessorFactory, runnableStarter, destinationSocketAddress);
        GatewayHttpConnectionFactoryFactory<HttpSyncSocket<HttpRequest>> factoryFactory = new DefaultGatewayHttpConnectionFactoryFactory<>(connectionFactory);
        HandlerFactory<HttpHandler, SyncSocket> handlerFactory = new DefaultGatewayHttpHandlerFactory<>(factoryFactory);
        SyncServerSocketFactory<SyncSocket> asyncServerSocketFactory = new DirectSyncServerSocketFactory(port, interfaceName, forceIpV4);
        DefaultProcessorFactory<HttpProcessingContext, HttpHandler> defaultProcessorFactory = new DefaultProcessorFactory<>(
                inputProcessorFactory, processingContextFactory);
		listener = new ScafaListener<>(asyncServerSocketFactory, defaultProcessorFactory,
				handlerFactory, runnableStarter);
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
