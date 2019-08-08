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
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.config.Configuration;
import com.github.apetrelli.scafa.http.HostPort;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.proxy.HttpConnectRequest;
import com.github.apetrelli.scafa.http.proxy.MappedProxyHttpConnectionFactory;
import com.github.apetrelli.scafa.http.proxy.ProxyHttpConnection;
import com.github.apetrelli.scafa.proto.aio.ResultHandler;

public class DefaultMappedProxyHttpConnectionFactory implements MappedProxyHttpConnectionFactory {

    private static final Logger LOG = Logger.getLogger(DefaultMappedProxyHttpConnectionFactory.class.getName());

    private Map<HostPort, ProxyHttpConnection> connectionCache = new HashMap<>();

    private Configuration configuration;

    public DefaultMappedProxyHttpConnectionFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void create(AsynchronousSocketChannel sourceChannel, HttpRequest request, ResultHandler<ProxyHttpConnection> handler) {
        try {
            create(sourceChannel, request.getHostPort(), handler);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Problem with determining the host to connect to.", e);
            handler.handle(new NullProxyHttpConnection(sourceChannel));
        }
    }

    @Override
    public void create(AsynchronousSocketChannel sourceChannel, HttpConnectRequest request, ResultHandler<ProxyHttpConnection> handler) {
        create(sourceChannel, new HostPort(request.getHost(), request.getPort()), handler);
    }

    @Override
    public void disconnectAll() throws IOException {
        connectionCache.values().stream().forEach(t -> closeQuietly(t));
        connectionCache.clear();
    }

    @Override
    public void dispose(HostPort target) {
        ProxyHttpConnection connection = connectionCache.get(target);
        if (connection != null) {
            connectionCache.remove(target);
        }
    }

    private void create(AsynchronousSocketChannel sourceChannel, HostPort hostPort, ResultHandler<ProxyHttpConnection> handler) {
        ProxyHttpConnection connection = connectionCache.get(hostPort);
        if (connection == null) {
            if (LOG.isLoggable(Level.INFO)) {
                LOG.log(Level.INFO, "Connecting thread {0} to address {1}",
                        new Object[] { Thread.currentThread().getName(), hostPort.toString() });
            }
            ProxyHttpConnection newConnection = configuration.getHttpConnectionFactoryByHost(hostPort.getHost()).create(this, sourceChannel,
                    hostPort);
            newConnection.ensureConnected(new CompletionHandler<Void, Void>() {

                @Override
                public void completed(Void result, Void attachment) {
                    connectionCache.put(hostPort, newConnection);
                    handler.handle(newConnection);
                }

                @Override
                public void failed(Throwable exc, Void attachment) {
                    LOG.log(Level.INFO, "Connection failed to " + hostPort.toString(), exc);
                    NullProxyHttpConnection nullConnection = new NullProxyHttpConnection(sourceChannel);
                    connectionCache.put(hostPort, nullConnection);
                    handler.handle(nullConnection);
                }
            });
        } else {
            handler.handle(connection);
        }
    }

    private void closeQuietly(ProxyHttpConnection connection) {
        try {
            connection.close();
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Error during closing a connection", e);
        }
    }

}
