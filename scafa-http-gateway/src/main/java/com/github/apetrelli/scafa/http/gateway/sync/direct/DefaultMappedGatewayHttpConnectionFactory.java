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
package com.github.apetrelli.scafa.http.gateway.sync.direct;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.gateway.sync.GatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.http.gateway.sync.MappedGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.sync.http.HttpSyncSocket;
import com.github.apetrelli.scafa.proto.IORuntimeException;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.sync.proto.SyncSocket;

public class DefaultMappedGatewayHttpConnectionFactory<T extends HttpSyncSocket<HttpRequest>> implements MappedGatewayHttpConnectionFactory<T> {

    private static final Logger LOG = Logger.getLogger(DefaultMappedGatewayHttpConnectionFactory.class.getName());

    private Map<HostPort, T> connectionCache = new HashMap<>();

    private GatewayHttpConnectionFactory<T> connectionFactory;

    public DefaultMappedGatewayHttpConnectionFactory(GatewayHttpConnectionFactory<T> connectionFactory) {
        this.connectionFactory = connectionFactory;
    }
    
    @Override
    public T create(SyncSocket sourceChannel, HttpRequest request) {
        try {
            return create(sourceChannel, request.getHostPort());
        } catch (IOException e) {
        	throw new IORuntimeException(e);
        }
    }

    @Override
    public void disconnectAll() throws IOException {
        connectionCache.values().stream().forEach(SyncSocket::disconnect);
        connectionCache.clear();
    }

    @Override
    public void dispose(HostPort target) {
    	T connection = connectionCache.get(target);
        if (connection != null) {
            connectionCache.remove(target);
        }
    }

    private T create(SyncSocket sourceChannel, HostPort hostPort) {
    	T connection = connectionCache.get(hostPort);
        if (connection == null) {
            if (LOG.isLoggable(Level.INFO)) {
                LOG.log(Level.INFO, "Connecting thread {0} to address {1}",
                        new Object[] { Thread.currentThread().getName(), hostPort });
            }
            connection = connectionFactory.create(this, sourceChannel, hostPort);
            connection.connect();
            connectionCache.put(hostPort, connection);
        }
        return connection;
    }
}
