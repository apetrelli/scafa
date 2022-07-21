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
package com.github.apetrelli.scafa.sync.http.gateway.handler;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.proto.Socket;
import com.github.apetrelli.scafa.proto.io.FlowBuffer;
import com.github.apetrelli.scafa.sync.http.HttpHandler;
import com.github.apetrelli.scafa.sync.http.HttpSyncSocket;
import com.github.apetrelli.scafa.sync.http.gateway.MappedGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.sync.http.impl.HttpHandlerSupport;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultGatewayHttpHandler<T extends HttpSyncSocket<HttpRequest>> extends HttpHandlerSupport implements HttpHandler {

    protected T connection;

    private final MappedGatewayHttpConnectionFactory<T> connectionFactory;

    private final Socket sourceChannel;
    
    @Override
    public void onResponseHeader(HttpResponse response) {
        throw new UnsupportedOperationException("Not expected a response header");
    }

    @Override
    public void onRequestHeader(HttpRequest request) {
    	connection = createConnection(request);
    	connection.sendHeader(request);
    }
    
    @Override
    public void onBody(FlowBuffer buffer, long offset, long length) {
        connection.sendData(buffer);
    }
    
    @Override
    public void onChunk(FlowBuffer buffer, long totalOffset, long chunkOffset, long chunkLength) {
        connection.sendData(buffer);
    }
    
    @Override
    public void onDataToPassAlong(FlowBuffer buffer) {
        connection.out().write(buffer).flush();
    }
    
    @Override
    public void onEnd() {
        connection.endData();
    }

    @Override
    public void onDisconnect() {
        if (connection != null) {
    		connection.disconnect(); // Ignore the outcome
        }
    }

	protected T createConnection(HttpRequest request) {
		return connectionFactory.create(sourceChannel, request);
	}
}
