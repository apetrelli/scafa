package com.github.apetrelli.scafa.http.gateway.direct;

import com.github.apetrelli.scafa.http.gateway.MappedGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.http.gateway.impl.AbstractGatewayHttpConnection;
import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.processor.Handler;
import com.github.apetrelli.scafa.proto.processor.Input;
import com.github.apetrelli.scafa.proto.processor.Processor;
import com.github.apetrelli.scafa.proto.processor.impl.DefaultProcessor;
import com.github.apetrelli.scafa.proto.processor.impl.PassthroughInputProcessorFactory;
import com.github.apetrelli.scafa.proto.processor.impl.SimpleInputFactory;

public abstract class AbstractDirectGatewayHttpConnection extends AbstractGatewayHttpConnection {


    private SimpleInputFactory inputFactory = new SimpleInputFactory();

    protected HostPort destinationSocketAddress;

    protected MappedGatewayHttpConnectionFactory factory;

	public AbstractDirectGatewayHttpConnection(AsyncSocket sourceChannel, AsyncSocket socket, MappedGatewayHttpConnectionFactory factory) {
		super(sourceChannel, socket);
		this.factory = factory;
	}

	@Override
	protected void prepareChannel() {
        Processor<Handler> processor = new DefaultProcessor<Input, Handler>(socket, new PassthroughInputProcessorFactory(sourceChannel), inputFactory);
        processor.process(new ChannelDisconnectorHandler(factory, socket, destinationSocketAddress));
	}

}
