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

import java.io.IOException;
import java.nio.channels.CompletionHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.proxy.HttpConnectRequest;
import com.github.apetrelli.scafa.http.proxy.HttpRequestManipulator;
import com.github.apetrelli.scafa.http.proxy.MappedProxyHttpConnectionFactory;
import com.github.apetrelli.scafa.http.proxy.ProxyHttpConnection;
import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.client.HostPort;

public abstract class AbstractUpstreamProxyHttpConnection extends AbstractProxyHttpConnection<AsyncSocket> implements ProxyHttpConnection {

    private static final Logger LOG = Logger.getLogger(AnonymousProxyHttpConnection.class.getName());

    protected HttpRequestManipulator manipulator;

    public AbstractUpstreamProxyHttpConnection(MappedProxyHttpConnectionFactory factory, AsyncSocket sourceChannel,
            HttpAsyncSocket<HttpRequest> socket, HostPort destinationSocketAddress, HttpRequestManipulator manipulator) {
        super(factory, sourceChannel, socket, destinationSocketAddress);
        this.manipulator = manipulator;
    }

    @Override
    public void connect(HttpConnectRequest request, CompletionHandler<Void, Void> completionHandler) {
        if (LOG.isLoggable(Level.INFO)) {
            LOG.log(Level.INFO, "Connected thread {0} to port {1} and host {2}:{3}", new Object[] {
                    Thread.currentThread().getName(), socket.getAddress().toString(), request.getHost(), request.getPort()});
        }
        doConnect(request, completionHandler);
    }

    protected void doConnect(HttpConnectRequest request, CompletionHandler<Void, Void> completionHandler) {
        socket.sendHeader(request, completionHandler);
    }

    @Override
    protected HttpRequest createForwardedRequest(HttpRequest request) throws IOException {
        if (manipulator != null) {
            request = new HttpRequest(request);
            manipulator.manipulate(request);
        }
        if (LOG.isLoggable(Level.INFO)) {
            LOG.log(Level.INFO, "Connected thread {0} to port {1} and URL {2}",
                    new Object[] { Thread.currentThread().getName(), socket.getAddress().toString(), request.getResource() });
        }
        return request;
    }

}
