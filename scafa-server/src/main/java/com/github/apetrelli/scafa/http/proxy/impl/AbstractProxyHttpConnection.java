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
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.http.HostPort;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.proxy.HttpConnectRequest;
import com.github.apetrelli.scafa.http.proxy.HttpRequestManipulator;
import com.github.apetrelli.scafa.http.proxy.MappedHttpConnectionFactory;
import com.github.apetrelli.scafa.util.HttpUtils;

public abstract class AbstractProxyHttpConnection extends AbstractHttpConnection {

    private static final Logger LOG = Logger.getLogger(AnonymousProxyHttpConnection.class.getName());

    protected HttpRequestManipulator manipulator;

    private HostPort proxySocketAddress;

    public AbstractProxyHttpConnection(MappedHttpConnectionFactory factory, AsynchronousSocketChannel sourceChannel,
            HostPort socketAddress, String interfaceName, boolean forceIpV4, HostPort proxySocketAddress, HttpRequestManipulator manipulator) {
        super(factory, sourceChannel, socketAddress, interfaceName, forceIpV4);
        this.proxySocketAddress = proxySocketAddress;
        this.manipulator = manipulator;
    }

    @Override
    protected void establishConnection(CompletionHandler<Void, Void> handler) {
        LOG.finest("Trying to connect to " + socketAddress.toString());
        channel.connect(new InetSocketAddress(proxySocketAddress.getHost(), proxySocketAddress.getPort()), null, handler);
    }

    @Override
    public void connect(HttpConnectRequest request, CompletionHandler<Void, Void> completionHandler) {
        if (LOG.isLoggable(Level.INFO)) {
            try {
                LOG.log(Level.INFO, "Connected thread {0} to port {1} and host {2}:{3}", new Object[] {
                        Thread.currentThread().getName(), channel.getLocalAddress().toString(), request.getHost(), request.getPort()});
            } catch (IOException e) {
                LOG.log(Level.SEVERE, "Cannot understand local address", e);
            }
        }
        doConnect(request, completionHandler);
    }

    protected void doConnect(HttpConnectRequest request, CompletionHandler<Void, Void> completionHandler) {
        HttpUtils.sendHeader(request, channel, completionHandler);
    }

    @Override
    protected HttpRequest createForwardedRequest(HttpRequest request) throws IOException {
        if (manipulator != null) {
            request = new HttpRequest(request);
            manipulator.manipulate(request);
        }
        if (LOG.isLoggable(Level.INFO)) {
            LOG.log(Level.INFO, "Connected thread {0} to port {1} and URL {2}",
                    new Object[] { Thread.currentThread().getName(), channel.getLocalAddress().toString(), request.getResource() });
        }
        return request;
    }

}
