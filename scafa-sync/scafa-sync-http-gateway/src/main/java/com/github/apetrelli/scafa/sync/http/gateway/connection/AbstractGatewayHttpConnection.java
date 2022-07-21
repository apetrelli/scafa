/**
 * Scafa - A universal non-caching proxy for the road warrior
 * Copyright (C) 2015  Antonio Petrelli
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.apetrelli.scafa.sync.http.gateway.connection;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.proto.Socket;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.io.FlowBuffer;
import com.github.apetrelli.scafa.proto.processor.Processor;
import com.github.apetrelli.scafa.proto.processor.ProcessorFactory;
import com.github.apetrelli.scafa.sync.http.HttpSyncSocket;
import com.github.apetrelli.scafa.sync.http.gateway.MappedGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.sync.http.gateway.handler.ChannelDisconnectorHandler;
import com.github.apetrelli.scafa.sync.proto.RunnableStarter;
import com.github.apetrelli.scafa.sync.proto.client.AbstractClientConnection;
import com.github.apetrelli.scafa.sync.proto.processor.DataHandler;

public abstract class AbstractGatewayHttpConnection<T extends Socket> extends AbstractClientConnection<HttpSyncSocket<HttpRequest>> implements HttpSyncSocket<HttpRequest> {

    protected final T sourceChannel;
    
    protected final ProcessorFactory<DataHandler, Socket> clientProcessorFactory;

    protected final HostPort destinationSocketAddress;

    protected final MappedGatewayHttpConnectionFactory<?> factory;
    
    private final RunnableStarter runnableStarter;

	public AbstractGatewayHttpConnection(MappedGatewayHttpConnectionFactory<?> factory,
			ProcessorFactory<DataHandler, Socket> clientProcessorFactory, RunnableStarter runnableStarter,
			T sourceChannel, HttpSyncSocket<HttpRequest> socket, HostPort destinationSocketAddress) {
		super(socket);
		this.clientProcessorFactory = clientProcessorFactory;
		this.sourceChannel = sourceChannel;
		this.factory = factory;
		this.destinationSocketAddress = destinationSocketAddress;
		this.runnableStarter = runnableStarter;
	}
	
	@Override
	public void sendHeader(HttpRequest request) {
        HttpRequest modifiedRequest;
        modifiedRequest = createForwardedRequest(request);
        doSendHeader(modifiedRequest);
    }
	
	@Override
	public void sendData(FlowBuffer buffer) {
    	socket.sendData(buffer);
    }
	
	@Override
	public void endData() {
    	socket.endData();
    }
    
    @Override
    public void disconnect() {
    	try {
    		super.disconnect();
    	} finally {
    		sourceChannel.disconnect();
    	}
    }

    protected abstract HttpRequest createForwardedRequest(HttpRequest request);

    protected void doSendHeader(HttpRequest request) {
        socket.sendHeader(request);
    }

	@Override
	protected void prepareChannel() {
        Processor<DataHandler> processor = clientProcessorFactory.create(socket);
		runnableStarter.start(() -> processor
				.process(new ChannelDisconnectorHandler(factory, sourceChannel, destinationSocketAddress)));
    }
}
