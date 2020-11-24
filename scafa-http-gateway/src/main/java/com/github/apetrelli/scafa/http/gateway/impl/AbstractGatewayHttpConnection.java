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
package com.github.apetrelli.scafa.http.gateway.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.gateway.MappedGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.http.gateway.direct.ChannelDisconnectorHandler;
import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.aio.CompletionHandlerFuture;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.client.impl.AbstractClientConnection;
import com.github.apetrelli.scafa.proto.processor.DataHandler;
import com.github.apetrelli.scafa.proto.processor.Processor;
import com.github.apetrelli.scafa.proto.processor.impl.DefaultProcessor;
import com.github.apetrelli.scafa.proto.processor.impl.PassthroughInputProcessorFactory;
import com.github.apetrelli.scafa.proto.processor.impl.SimpleInputFactory;

public abstract class AbstractGatewayHttpConnection<T extends AsyncSocket> extends AbstractClientConnection<HttpAsyncSocket<HttpRequest>> implements HttpAsyncSocket<HttpRequest> {
	
	private static final Logger LOG = Logger.getLogger(AbstractGatewayHttpConnection.class.getName());

    protected T sourceChannel;

    private SimpleInputFactory inputFactory = new SimpleInputFactory();

    protected HostPort destinationSocketAddress;

    protected MappedGatewayHttpConnectionFactory<?> factory;

	public AbstractGatewayHttpConnection(T sourceChannel, HttpAsyncSocket<HttpRequest> socket, MappedGatewayHttpConnectionFactory<?> factory) {
		super(socket);
		this.sourceChannel = sourceChannel;
		this.factory = factory;
	}
	
	@Override
	public CompletableFuture<Void> sendHeader(HttpRequest request) {
        HttpRequest modifiedRequest;
        try {
            modifiedRequest = createForwardedRequest(request);
            return doSendHeader(modifiedRequest);
        } catch (IOException e) {
            return CompletableFuture.failedFuture(e);
        }
    }
	
	@Override
	public CompletableFuture<Void> sendData(ByteBuffer buffer) {
    	return socket.sendData(buffer);
    }
	
	@Override
	public CompletableFuture<Void> endData() {
    	return socket.endData();
    }
	
	@Override
	public CompletableFuture<Void> disconnect() {
		return super.disconnect().handle((r, e) -> {
			LOG.log(Level.SEVERE, "Cannot disconnect gateway client channel", e);
			return CompletionHandlerFuture.completeEmpty();
		}).thenCompose(x -> sourceChannel.disconnect());
    }

    protected abstract HttpRequest createForwardedRequest(HttpRequest request) throws IOException;

    protected CompletableFuture<Void> doSendHeader(HttpRequest request) {
        return socket.sendHeader(request);
    }

	@Override
	protected void prepareChannel() {
        Processor<DataHandler> processor = new DefaultProcessor<>(socket, new PassthroughInputProcessorFactory(), inputFactory);
        processor.process(new ChannelDisconnectorHandler(factory, socket, destinationSocketAddress));
	}
}
