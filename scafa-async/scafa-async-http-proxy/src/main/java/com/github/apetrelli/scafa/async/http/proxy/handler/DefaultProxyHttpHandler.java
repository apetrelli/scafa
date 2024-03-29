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
package com.github.apetrelli.scafa.async.http.proxy.handler;

import static com.github.apetrelli.scafa.http.HttpHeaders.CONNECT;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.async.http.gateway.MappedGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.async.http.gateway.handler.DefaultGatewayHttpHandler;
import com.github.apetrelli.scafa.async.http.proxy.ProxyHttpConnection;
import com.github.apetrelli.scafa.async.proto.socket.AsyncSocket;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.proxy.HttpConnectRequest;

public class DefaultProxyHttpHandler extends DefaultGatewayHttpHandler<ProxyHttpConnection> implements ProxyHttpHandler {
	
    public DefaultProxyHttpHandler(MappedGatewayHttpConnectionFactory<ProxyHttpConnection> connectionFactory, AsyncSocket sourceChannel) {
    	super(connectionFactory, sourceChannel);
    }

    @Override
    public CompletableFuture<Void> onResponseHeader(HttpResponse response) {
    	return CompletableFuture.failedFuture(new UnsupportedOperationException("Not expected a response header"));
    }

    @Override
    public CompletableFuture<Void> onRequestHeader(HttpRequest request) {
        if (CONNECT.equals(request.getMethod())) {
            HttpConnectRequest connectRequest;
            try {
                connectRequest = new HttpConnectRequest(request);
                return onConnectMethod(connectRequest);
            } catch (IOException e) {
            	return CompletableFuture.failedFuture(e);
            }
        } else {
			return super.onRequestHeader(request);
        }
    }
    
    @Override
    public CompletableFuture<Void> onConnectMethod(HttpConnectRequest connectRequest) {
		return createConnection(connectRequest).thenCompose(x -> x.connect(connectRequest, writeBuffer));
    }
}
