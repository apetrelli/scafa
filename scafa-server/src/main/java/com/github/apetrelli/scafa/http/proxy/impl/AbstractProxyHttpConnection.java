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
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpRequestManipulator;
import com.github.apetrelli.scafa.http.impl.HostPort;
import com.github.apetrelli.scafa.http.proxy.HttpConnectRequest;
import com.github.apetrelli.scafa.util.HttpUtils;

public abstract class AbstractProxyHttpConnection extends AbstractHttpConnection {

    private static final Logger LOG = Logger.getLogger(AnonymousProxyHttpConnection.class.getName());

    protected HttpRequestManipulator manipulator;

    public AbstractProxyHttpConnection(AsynchronousSocketChannel sourceChannel, String host, int port,
            HttpRequestManipulator manipulator) throws IOException {
        super(sourceChannel);
        this.manipulator = manipulator;
        HostPort socketAddress = new HostPort(host, port);
        LOG.finest("Trying to connect to " + socketAddress.toString());
        HttpUtils.getFuture(channel.connect(new InetSocketAddress(socketAddress.getHost(), socketAddress.getPort())));
        if (LOG.isLoggable(Level.INFO)) {
            LOG.log(Level.INFO, "Connected thread {0} to port {1}",
                    new Object[] { Thread.currentThread().getName(), channel.getLocalAddress().toString() });
        }
    }

    @Override
    public void connect(HttpConnectRequest request) throws IOException {
        if (LOG.isLoggable(Level.INFO)) {
            LOG.log(Level.INFO, "Connected thread {0} to port {1} and host {2}:{3}", new Object[] {
                    Thread.currentThread().getName(), channel.getLocalAddress().toString(), request.getHost(), request.getPort()});
        }
        doConnect(request);
    }

    protected void doConnect(HttpConnectRequest request) throws IOException {
        HttpUtils.sendHeader(request, channel);
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