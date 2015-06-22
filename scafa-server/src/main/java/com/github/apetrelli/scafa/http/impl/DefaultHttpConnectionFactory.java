/**
 * Scafa - Universal roadwarrior non-caching proxy
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
package com.github.apetrelli.scafa.http.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ini4j.Profile.Section;

import com.github.apetrelli.scafa.config.Configuration;
import com.github.apetrelli.scafa.http.HttpConnection;
import com.github.apetrelli.scafa.http.HttpConnectionFactory;
import com.github.apetrelli.scafa.http.ntlm.NtlmProxyHttpConnection;

public class DefaultHttpConnectionFactory implements HttpConnectionFactory {

    private static final Map<String, Integer> protocol2port = new HashMap<String, Integer>();

    private static final Logger LOG = Logger.getLogger(DefaultHttpConnectionFactory.class.getName());

    static {
        protocol2port.put("http", 80);
        protocol2port.put("https", 443);
    }

    private Map<HostPort, HttpConnection> connectionCache = new HashMap<>();
    
    private Configuration configuration;
    
    public DefaultHttpConnectionFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public HttpConnection create(AsynchronousSocketChannel sourceChannel, String method, String url,
            Map<String, List<String>> headers, String httpVersion) throws IOException {
        HostPort hostPort = getHostToConnect(url, headers);
        return create(sourceChannel, hostPort);
    }
    
    @Override
    public HttpConnection create(AsynchronousSocketChannel sourceChannel, String method, String host, int port,
            Map<String, List<String>> headers, String httpVersion) throws IOException {
        return create(sourceChannel, new HostPort(host, port));
    }

    @Override
    public void disconnectAll() throws IOException {
        connectionCache.values().stream().forEach(t -> closeQuietly(t));
        connectionCache.clear();
    }

    @Override
    public void dispose(HostPort target) throws IOException {
        HttpConnection connection = connectionCache.get(target);
        if (connection != null) {
            connectionCache.remove(target);
        }
    }

    private HttpConnection create(AsynchronousSocketChannel sourceChannel, HostPort hostPort) throws IOException {
        HttpConnection connection = connectionCache.get(hostPort);
        if (connection == null || !connection.isOpen()) {
            Section section = configuration.getConfigurationByHost(hostPort.getHost());
            String type = section.get("type");
            if (section != null && type != null) {
                switch (type) {
                case "ntlm":
                    connection = new NtlmProxyHttpConnection(this, sourceChannel, section);
                }
            }
            if (connection == null) {
                connection = new DirectHttpConnection(this, sourceChannel, hostPort);
            }
            connectionCache.put(hostPort, connection);
        }
        return connection;
    }

    private HostPort getHostToConnect(String url, Map<String, List<String>> headers) throws IOException {
        HostPort retValue;
        List<String> hosts = headers.get("HOST");
        if (hosts != null) {
            if (hosts.size() == 1) {
                String hostString = hosts.iterator().next();
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
                throw new IOException("Multiple hosts defined: " + hosts.toString());
            }
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
