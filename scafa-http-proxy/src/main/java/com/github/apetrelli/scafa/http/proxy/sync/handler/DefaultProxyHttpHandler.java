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
package com.github.apetrelli.scafa.http.proxy.sync.handler;

import static com.github.apetrelli.scafa.http.HttpHeaders.CONNECT;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.github.apetrelli.scafa.http.HttpException;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.proxy.HttpConnectRequest;
import com.github.apetrelli.scafa.http.proxy.sync.MappedProxyHttpConnectionFactory;
import com.github.apetrelli.scafa.http.proxy.sync.ProxyHttpConnection;
import com.github.apetrelli.scafa.sync.http.gateway.handler.DefaultGatewayHttpHandler;
import com.github.apetrelli.scafa.sync.proto.SyncSocket;

public class DefaultProxyHttpHandler extends DefaultGatewayHttpHandler<ProxyHttpConnection> implements ProxyHttpHandler {

    private MappedProxyHttpConnectionFactory connectionFactory;

    public DefaultProxyHttpHandler(MappedProxyHttpConnectionFactory connectionFactory, SyncSocket sourceChannel) {
    	super(connectionFactory, sourceChannel);
        this.connectionFactory = connectionFactory;
    }

    @Override
    public void onResponseHeader(HttpResponse response) {
    	throw new UnsupportedOperationException("Not expected a response header");
    }

    @Override
    public void onRequestHeader(HttpRequest request) {
        if (CONNECT.equals(request.getMethod())) {
            HttpConnectRequest connectRequest;
            try {
                connectRequest = new HttpConnectRequest(request);
                onConnectMethod(connectRequest);
            } catch (IOException e) {
            	throw new HttpException(e);
            }
        } else {
			connection = connectionFactory.create(sourceChannel, request);
			connection.sendHeader(request, writeBuffer);
        }
    }
    
    @Override
    public void onBody(ByteBuffer buffer, long offset, long length) {
        connection.sendData(buffer);
    }
    
    @Override
    public void onChunk(ByteBuffer buffer, long totalOffset, long chunkOffset, long chunkLength) {
        connection.sendData(buffer);
    }
    
    @Override
    public void onConnectMethod(HttpConnectRequest connectRequest) {
		connection = connectionFactory.create(sourceChannel, connectRequest);
		connection.connect(connectRequest, writeBuffer);
    }
    
    @Override
    public void onDataToPassAlong(ByteBuffer buffer) {
        connection.flushBuffer(buffer);
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
}
