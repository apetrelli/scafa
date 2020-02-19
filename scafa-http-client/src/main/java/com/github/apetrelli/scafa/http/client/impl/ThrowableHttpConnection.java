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
package com.github.apetrelli.scafa.http.client.impl;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.client.HttpClientConnection;
import com.github.apetrelli.scafa.http.client.HttpClientHandler;
import com.github.apetrelli.scafa.proto.client.HostPort;

public class ThrowableHttpConnection implements HttpClientConnection {

	private Throwable throwable;
	
	private HttpClientHandler clientHandler;

    public ThrowableHttpConnection(Throwable throwable) {
		this.throwable = throwable;
	}

	@Override
    public void connect(CompletionHandler<Void, Void> handler) {
        handler.completed(null, null);
    }
	
	@Override
	public void prepare(HttpRequest request, HttpClientHandler clientHandler) {
	    this.clientHandler = clientHandler;
	}

	@Override
	public void sendHeader(HttpRequest request, CompletionHandler<Void, Void> completionHandler) {
        clientHandler.onRequestError(request, throwable);
        completionHandler.failed(throwable, null);
    }
	
	@Override
	public void sendData(ByteBuffer buffer, CompletionHandler<Void, Void> completionHandler) {
        buffer.clear();
        completionHandler.completed(null, null);
	}

    @Override
    public void flushBuffer(ByteBuffer buffer, CompletionHandler<Void, Void> completionHandler) {
        buffer.clear();
        completionHandler.completed(null, null);
    }

    @Override
    public void endData(CompletionHandler<Void, Void> completionHandler) {
        completionHandler.completed(null, null);
    }
    
    @Override
    public void disconnect(CompletionHandler<Void, Void> handler) {
        handler.completed(null, null);
    }

    @Override
    public HostPort getAddress() {
        return null;
    }

    @Override
    public <A> void read(ByteBuffer buffer, A attachment, CompletionHandler<Integer, ? super A> handler) {
        handler.completed(null, null);
    }

    @Override
    public <A> void write(ByteBuffer buffer, A attachment, CompletionHandler<Integer, ? super A> handler) {
        handler.completed(null, null);
    }

    @Override
    public boolean isOpen() {
        return false;
    }

}
