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
import java.net.MalformedURLException;
import java.net.URL;
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
import com.github.apetrelli.scafa.http.proxy.HttpConnection;
import com.github.apetrelli.scafa.http.proxy.MappedHttpConnectionFactory;
import com.github.apetrelli.scafa.http.proxy.ResultHandler;

public class DefaultMappedHttpConnectionFactory implements MappedHttpConnectionFactory {

    private static final Map<String, Integer> protocol2port = new HashMap<String, Integer>();

    private static final Logger LOG = Logger.getLogger(DefaultMappedHttpConnectionFactory.class.getName());

    static {
        protocol2port.put("http", 80);
        protocol2port.put("https", 443);
        protocol2port.put("ftp", 80); // This works only with a proxy.
    }

    private Map<HostPort, HttpConnection> connectionCache = new HashMap<>();

    private Configuration configuration;

    public DefaultMappedHttpConnectionFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void create(AsynchronousSocketChannel sourceChannel, HttpRequest request, ResultHandler<HttpConnection> handler) {
        try {
            HostPort hostPort = getHostToConnect(request);
            create(sourceChannel, hostPort, handler);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Problem with determining the host to connect to.", e);
            handler.handle(new NullHttpConnection(sourceChannel));
        }
    }

    @Override
    public void create(AsynchronousSocketChannel sourceChannel, HttpConnectRequest request, ResultHandler<HttpConnection> handler) {
        create(sourceChannel, new HostPort(request.getHost(), request.getPort()), handler);
    }

    @Override
    public void disconnectAll() throws IOException {
        connectionCache.values().stream().forEach(t -> closeQuietly(t));
        connectionCache.clear();
    }

    @Override
    public void dispose(HostPort target) {
        HttpConnection connection = connectionCache.get(target);
        if (connection != null) {
            connectionCache.remove(target);
        }
    }

    private void create(AsynchronousSocketChannel sourceChannel, HostPort hostPort, ResultHandler<HttpConnection> handler) {
        HttpConnection connection = connectionCache.get(hostPort);
        if (connection == null) {
            if (LOG.isLoggable(Level.INFO)) {
                LOG.log(Level.INFO, "Connecting thread {0} to address {1}",
                        new Object[] { Thread.currentThread().getName(), hostPort.toString() });
            }
            HttpConnection newConnection = configuration.getHttpConnectionFactoryByHost(hostPort.getHost()).create(this, sourceChannel,
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
                    NullHttpConnection nullConnection = new NullHttpConnection(sourceChannel);
                    connectionCache.put(hostPort, nullConnection);
                    handler.handle(nullConnection);
                }
            });
        } else {
            handler.handle(connection);
        }
    }

    private HostPort getHostToConnect(HttpRequest request) throws IOException {
        HostPort retValue;
        String hostString = request.getHeader("HOST");
        String url = request.getResource();
        if (hostString != null) {
            String[] hostStringSplit = hostString.split(":");
            Integer port = null;
            if (hostStringSplit.length == 1) {
                try {
                    URL realUrl = new URL(url);
                    port = protocol2port.get(realUrl.getProtocol());
                } catch (MalformedURLException e) {
                    // Rare, only in HTTP 1.0
                    LOG.log(Level.FINE, "Host header not present and connect executed!", e);
                    hostStringSplit = url.split(":");
                    if (hostStringSplit.length != 2) {
                        throw new IOException("Malformed Host url: " + url);
                    }
                }
            } else if (hostStringSplit.length != 2) {
                throw new IOException("Malformed Host header: " + hostString);
            }
            if (port == null) {
                try {
                    port = Integer.decode(hostStringSplit[1]);
                } catch (NumberFormatException e) {
                    throw new IOException("Malformed port: " + hostStringSplit[1], e);
                }
            }
            if (port == null || port < 0) {
                throw new IOException("Invalid port " + port + " for connection to " + url);
            }
            retValue = new HostPort(hostStringSplit[0], port);
        } else {
            URL realUrl = new URL(url);
            retValue = new HostPort(realUrl.getHost(), realUrl.getPort());
        }
        return retValue;
    }

    private void closeQuietly(HttpConnection connection) {
        try {
            connection.close();
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Error during closing a connection", e);
        }
    }

}
