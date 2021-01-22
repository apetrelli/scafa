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
package com.github.apetrelli.scafa.http.proxy.impl;

import static com.github.apetrelli.scafa.http.HttpHeaders.OK;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.http.HttpCodes;
import com.github.apetrelli.scafa.http.HttpException;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.async.HttpAsyncSocket;
import com.github.apetrelli.scafa.http.gateway.MappedGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.http.gateway.impl.AbstractGatewayHttpConnection;
import com.github.apetrelli.scafa.http.proxy.HttpConnectRequest;
import com.github.apetrelli.scafa.http.proxy.ProxyHttpConnection;
import com.github.apetrelli.scafa.proto.async.processor.DataHandler;
import com.github.apetrelli.scafa.proto.async.socket.AsyncSocket;
import com.github.apetrelli.scafa.proto.processor.ProcessorFactory;
import com.github.apetrelli.scafa.proto.util.AsciiString;

public class DirectProxyHttpConnection extends AbstractGatewayHttpConnection<HttpAsyncSocket<HttpResponse>> implements ProxyHttpConnection {

	private static final Logger LOG = Logger.getLogger(DirectProxyHttpConnection.class.getName());

    public DirectProxyHttpConnection(MappedGatewayHttpConnectionFactory<?> factory,
    		ProcessorFactory<DataHandler, AsyncSocket> clientProcessorFactory,
            HttpAsyncSocket<HttpResponse> sourceChannel, HttpAsyncSocket<HttpRequest> socket) {
        super(factory, clientProcessorFactory, sourceChannel, socket, socket.getAddress());
    }
    
    @Override
    public CompletableFuture<Void> connect(HttpConnectRequest request, ByteBuffer writeBuffer) {
        // Already connected, need only to send a 200.
        AsciiString httpVersion = request.getHttpVersion();
        HttpResponse response = new HttpResponse(httpVersion, HttpCodes.OK, OK);
        return sourceChannel.sendHeader(response, writeBuffer).thenCompose(x -> sourceChannel.endData());
    }

    protected HttpRequest createForwardedRequest(HttpRequest request) {
        URL realurl;
		try {
			realurl = new URL(request.getResource().toString());
		} catch (MalformedURLException e) {
			throw new HttpException(e);
		}
        HttpRequest modifiedRequest = new HttpRequest(request);
        modifiedRequest.setResource(new AsciiString(realurl.getFile()));
        if (LOG.isLoggable(Level.INFO)) {
            LOG.log(Level.INFO, "Direct connection: connected thread {0} to port {1} and URL {2}",
                    new Object[] { Thread.currentThread().getName(), socket.getAddress(), request.getResource() });
        }
        return modifiedRequest;
    }

}
