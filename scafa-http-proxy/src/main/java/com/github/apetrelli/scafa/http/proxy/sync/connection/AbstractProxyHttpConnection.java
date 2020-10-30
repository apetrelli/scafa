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
package com.github.apetrelli.scafa.http.proxy.sync.connection;

import java.nio.ByteBuffer;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.proxy.sync.MappedProxyHttpConnectionFactory;
import com.github.apetrelli.scafa.http.proxy.sync.ProxyHttpConnection;
import com.github.apetrelli.scafa.http.proxy.sync.handler.ChannelDisconnectorHandler;
import com.github.apetrelli.scafa.http.sync.HttpSyncSocket;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.processor.Processor;
import com.github.apetrelli.scafa.proto.processor.ProcessorFactory;
import com.github.apetrelli.scafa.proto.sync.SyncSocket;
import com.github.apetrelli.scafa.proto.sync.client.AbstractClientConnection;
import com.github.apetrelli.scafa.proto.sync.processor.DataHandler;

public abstract class AbstractProxyHttpConnection<T extends SyncSocket> extends AbstractClientConnection<HttpSyncSocket<HttpRequest>> implements ProxyHttpConnection {

    protected MappedProxyHttpConnectionFactory factory;
    
    protected ProcessorFactory<DataHandler, SyncSocket> clientProcessorFactory;

    protected T sourceChannel;

    private HostPort destinationSocketAddress;

	public AbstractProxyHttpConnection(MappedProxyHttpConnectionFactory factory,
			ProcessorFactory<DataHandler, SyncSocket> clientProcessorFactory, T sourceChannel,
			HttpSyncSocket<HttpRequest> socket, HostPort destinationSocketAddress) {
        super(socket);
        this.factory = factory;
        this.clientProcessorFactory = clientProcessorFactory;
        this.sourceChannel = sourceChannel;
        this.destinationSocketAddress = destinationSocketAddress;
    }
	
	@Override
	public void sendHeader(HttpRequest request) {
        HttpRequest modifiedRequest;
        modifiedRequest = createForwardedRequest(request);
        doSendHeader(modifiedRequest);
    }
	
	@Override
	public void sendData(ByteBuffer buffer) {
    	socket.sendData(buffer);
    }
	
	@Override
	public void endData() {
    	socket.endData();
    }

    protected abstract HttpRequest createForwardedRequest(HttpRequest request);
    
    @Override
    public void disconnect() {
    	try {
    		super.disconnect();
    	} finally {
    		sourceChannel.disconnect();
    	}
    }

    protected void doSendHeader(HttpRequest request) {
        socket.sendHeader(request);
    }

    protected void prepareChannel() {
        Processor<DataHandler> processor = clientProcessorFactory.create(socket);
        processor.process(new ChannelDisconnectorHandler(factory, sourceChannel, destinationSocketAddress));
    }
}
