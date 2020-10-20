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
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.client.HttpClientConnection;
import com.github.apetrelli.scafa.http.client.HttpClientHandler;
import com.github.apetrelli.scafa.proto.aio.CompletionHandlerFuture;
import com.github.apetrelli.scafa.proto.client.HostPort;

public class ThrowableHttpConnection implements HttpClientConnection {

	private Throwable throwable;
	
	private HttpClientHandler clientHandler;

    public ThrowableHttpConnection(Throwable throwable) {
		this.throwable = throwable;
	}
    
    @Override
    public CompletableFuture<Void> connect() {
		return CompletionHandlerFuture.completeEmpty();
    }
	
	@Override
	public void prepare(HttpRequest request, HttpClientHandler clientHandler) {
	    this.clientHandler = clientHandler;
	}
	
	@Override
	public CompletableFuture<Void> sendHeader(HttpRequest request) {
        clientHandler.onRequestError(request, throwable);
        return CompletableFuture.failedFuture(throwable);
    }
	
	@Override
	public CompletableFuture<Void> sendData(ByteBuffer buffer) {
        buffer.clear();
		return CompletionHandlerFuture.completeEmpty();
	}

	@Override
	public CompletableFuture<Void> flushBuffer(ByteBuffer buffer) {
        buffer.clear();
		return CompletionHandlerFuture.completeEmpty();
    }
	
	@Override
	public CompletableFuture<Void> endData() {
		return CompletionHandlerFuture.completeEmpty();
	}
	
	@Override
	public CompletableFuture<Void> disconnect() {
		return CompletionHandlerFuture.completeEmpty();
	}

    @Override
    public HostPort getAddress() {
        return null;
    }
    
    @Override
    public CompletableFuture<Integer> read(ByteBuffer buffer) {
		return CompletableFuture.completedFuture(-1);
    }

    @Override
    public CompletableFuture<Integer> write(ByteBuffer buffer) {
		return CompletableFuture.completedFuture(-1);
    }

    @Override
    public boolean isOpen() {
        return false;
    }

}
