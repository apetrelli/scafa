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
import java.nio.channels.CompletionHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.client.impl.AbstractClientConnection;

public abstract class AbstractGatewayHttpConnection extends AbstractClientConnection<HttpAsyncSocket<HttpRequest>> implements HttpAsyncSocket<HttpRequest> {
	
	private static final Logger LOG = Logger.getLogger(AbstractGatewayHttpConnection.class.getName());

    protected AsyncSocket sourceChannel;

	public AbstractGatewayHttpConnection(AsyncSocket sourceChannel, HttpAsyncSocket<HttpRequest> socket) {
        super(socket);
        this.sourceChannel = sourceChannel;
    }

    @Override
    public void sendHeader(HttpRequest request, CompletionHandler<Void, Void> completionHandler) {
        HttpRequest modifiedRequest;
        try {
            modifiedRequest = createForwardedRequest(request);
            doSendHeader(modifiedRequest, completionHandler);
        } catch (IOException e) {
            completionHandler.failed(e, null);
        }
    }
    
    @Override
    public void sendData(ByteBuffer buffer, CompletionHandler<Void, Void> completionHandler) {
    	socket.sendData(buffer, completionHandler);
    }

    @Override
    public void endData(CompletionHandler<Void, Void> completionHandler) {
    	socket.endData(completionHandler);
    }
    
    @Override
    public void disconnect(CompletionHandler<Void, Void> handler) {
    	super.disconnect(new CompletionHandler<Void, Void>() {

			@Override
			public void completed(Void result, Void attachment) {
				sourceChannel.disconnect(handler);
			}

			@Override
			public void failed(Throwable exc, Void attachment) {
				LOG.log(Level.SEVERE, "Cannot disconnect gateway client channel", exc);
				sourceChannel.disconnect(handler);
			}
		});
    }

    protected abstract HttpRequest createForwardedRequest(HttpRequest request) throws IOException;

    protected void doSendHeader(HttpRequest request, CompletionHandler<Void, Void> completionHandler) {
        socket.sendHeader(request, completionHandler);
    }
}
