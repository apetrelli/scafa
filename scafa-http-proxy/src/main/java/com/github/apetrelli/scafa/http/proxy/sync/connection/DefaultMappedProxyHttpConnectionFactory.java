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
package com.github.apetrelli.scafa.http.proxy.sync.connection;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.http.HttpException;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.proxy.HttpConnectRequest;
import com.github.apetrelli.scafa.http.proxy.sync.MappedProxyHttpConnectionFactory;
import com.github.apetrelli.scafa.http.proxy.sync.ProxyHttpConnection;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.sync.http.gateway.GatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.sync.proto.SyncSocket;

public class DefaultMappedProxyHttpConnectionFactory implements MappedProxyHttpConnectionFactory {

    private static final Logger LOG = Logger.getLogger(DefaultMappedProxyHttpConnectionFactory.class.getName());

    private Map<HostPort, ProxyHttpConnection> connectionCache = new HashMap<>();

    private GatewayHttpConnectionFactory<ProxyHttpConnection> connectionFactory;

    public DefaultMappedProxyHttpConnectionFactory(GatewayHttpConnectionFactory<ProxyHttpConnection> connectionFactory) {
        this.connectionFactory = connectionFactory;
    }
    
    @Override
    public ProxyHttpConnection create(SyncSocket sourceChannel, HttpRequest request) {
        try {
            return create(sourceChannel, request.getHostPort());
        } catch (IOException e) {
            throw new HttpException("Problem with determining the host to connect to.", e);
        }
    }

    @Override
    public ProxyHttpConnection create(SyncSocket sourceChannel, HttpConnectRequest request) {
        return create(sourceChannel, new HostPort(request.getHost(), request.getPort()));
    }

    @Override
    public void disconnectAll() throws IOException {
		connectionCache.values().stream().forEach(SyncSocket::disconnect); // Ignore the outcome.
        connectionCache.clear();
    }

    @Override
    public void dispose(HostPort target) {
        ProxyHttpConnection connection = connectionCache.get(target);
        if (connection != null) {
            connectionCache.remove(target);
        }
    }

    private ProxyHttpConnection create(SyncSocket sourceChannel, HostPort hostPort) {
        ProxyHttpConnection connection = connectionCache.get(hostPort);
        if (connection == null) {
            if (LOG.isLoggable(Level.INFO)) {
                LOG.log(Level.INFO, "Connecting thread {0} to address {1}",
                        new Object[] { Thread.currentThread().getName(), hostPort });
            }
            ProxyHttpConnection newConnection = connectionFactory.create(this, sourceChannel, hostPort);
            newConnection.connect();
            connectionCache.put(hostPort, newConnection);
            return newConnection;
        } else {
            return connection;
        }
    }
}
