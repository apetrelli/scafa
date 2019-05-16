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
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.impl.HostPort;
import com.github.apetrelli.scafa.http.proxy.HttpConnectRequest;
import com.github.apetrelli.scafa.http.proxy.MappedHttpConnectionFactory;
import com.github.apetrelli.scafa.util.HttpUtils;

public class DirectHttpConnection extends AbstractHttpConnection {

	private static final Logger LOG = Logger.getLogger(DirectHttpConnection.class.getName());

    public DirectHttpConnection(MappedHttpConnectionFactory factory,
            AsynchronousSocketChannel sourceChannel, HostPort socketAddress)
            throws IOException {
        super(sourceChannel);
        HttpUtils.getFuture(channel.connect(new InetSocketAddress(socketAddress.getHost(), socketAddress.getPort())));
        prepareChannel(factory, sourceChannel, socketAddress);
    }

    protected HttpRequest createForwardedRequest(HttpRequest request) throws IOException {
        URL realurl = new URL(request.getResource());
        HttpRequest modifiedRequest = new HttpRequest(request);
        modifiedRequest.setResource(realurl.getFile());
        if (LOG.isLoggable(Level.INFO)) {
            LOG.log(Level.INFO, "Direct connection: connected thread {0} to port {1} and URL {2}",
                    new Object[] { Thread.currentThread().getName(), channel.getLocalAddress().toString(), request.getResource() });
        }
        return modifiedRequest;
    }

    @Override
    public void connect(HttpConnectRequest request) throws IOException {
        Charset charset = StandardCharsets.US_ASCII;
        // Already connected, need only to send a 200.
        String httpVersion = request.getHttpVersion();
        ByteBuffer buffer = ByteBuffer.allocate(httpVersion.length() + 11);
        buffer.put(httpVersion.getBytes(charset)).put(SPACE).put("200".getBytes(charset)).put(SPACE)
                .put("OK".getBytes(charset)).put(CR).put(LF).put(CR).put(LF);
        buffer.flip();
        HttpUtils.getFuture(sourceChannel.write(buffer));
        buffer.clear();
    }

}
