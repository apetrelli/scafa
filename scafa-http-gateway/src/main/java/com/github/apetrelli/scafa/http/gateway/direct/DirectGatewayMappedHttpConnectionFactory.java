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
package com.github.apetrelli.scafa.http.gateway.direct;

import java.io.IOException;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.http.HttpConnection;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.gateway.GatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.http.gateway.MappedGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.tls.util.IOUtils;

public class DirectGatewayMappedHttpConnectionFactory implements MappedGatewayHttpConnectionFactory {

    private static final Logger LOG = Logger.getLogger(DirectGatewayMappedHttpConnectionFactory.class.getName());

    private Map<HostPort, HttpConnection> connectionCache = new HashMap<>();

    private GatewayHttpConnectionFactory connectionFactory;

    public DirectGatewayMappedHttpConnectionFactory(GatewayHttpConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public void create(AsyncSocket sourceChannel, HttpRequest request, CompletionHandler<HttpConnection, Void> handler) {
        try {
            create(sourceChannel, request.getHostPort(), handler);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Problem with determining the host to connect to.", e);
            handler.failed(e, null);
        }
    }

    @Override
    public void disconnectAll() throws IOException {
        connectionCache.values().stream().forEach(t -> IOUtils.closeQuietly(t));
        connectionCache.clear();
    }

    @Override
    public void dispose(HostPort target) {
        HttpConnection connection = connectionCache.get(target);
        if (connection != null) {
            connectionCache.remove(target);
        }
    }

    private void create(AsyncSocket sourceChannel, HostPort hostPort, CompletionHandler<HttpConnection, Void> handler) {
    	HttpConnection connection = connectionCache.get(hostPort);
        if (connection == null) {
            if (LOG.isLoggable(Level.INFO)) {
                LOG.log(Level.INFO, "Connecting thread {0} to address {1}",
                        new Object[] { Thread.currentThread().getName(), hostPort.toString() });
            }
			HttpConnection newConnection = connectionFactory.create(this, sourceChannel, hostPort);
            newConnection.ensureConnected(new CompletionHandler<Void, Void>() {

                @Override
                public void completed(Void result, Void attachment) {
                    connectionCache.put(hostPort, newConnection);
                    handler.completed(newConnection, attachment);
                }

                @Override
                public void failed(Throwable exc, Void attachment) {
                    LOG.log(Level.INFO, "Connection failed to " + hostPort.toString(), exc);
                    handler.failed(exc, attachment);
                }
            });
        } else {
            handler.completed(connection, null);
        }
    }
}
