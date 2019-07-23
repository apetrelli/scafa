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
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.http.HostPort;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.proxy.HttpConnectRequest;
import com.github.apetrelli.scafa.http.proxy.MappedHttpConnectionFactory;
import com.github.apetrelli.scafa.proto.aio.DelegateFailureCompletionHandler;

public class DirectHttpConnection extends AbstractHttpConnection {

    private static final Logger LOG = Logger.getLogger(DirectHttpConnection.class.getName());

    public DirectHttpConnection(MappedHttpConnectionFactory factory,
            AsynchronousSocketChannel sourceChannel, HostPort socketAddress, String interfaceName, boolean forceIpV4) {
        super(factory, sourceChannel, socketAddress, interfaceName, forceIpV4);
    }

    @Override
    protected void establishConnection(CompletionHandler<Void, Void> handler) {
        channel.connect(new InetSocketAddress(socketAddress.getHost(), socketAddress.getPort()), null, handler);
    }

    @Override
    public void connect(HttpConnectRequest request, CompletionHandler<Void, Void> completionHandler) {
        Charset charset = StandardCharsets.US_ASCII;
        // Already connected, need only to send a 200.
        String httpVersion = request.getHttpVersion();
        ByteBuffer buffer = ByteBuffer.allocate(httpVersion.length() + 11);
        buffer.put(httpVersion.getBytes(charset)).put(SPACE).put("200".getBytes(charset)).put(SPACE)
                .put("OK".getBytes(charset)).put(CR).put(LF).put(CR).put(LF);
        buffer.flip();
        sourceChannel.write(buffer, null, new DelegateFailureCompletionHandler<Integer, Void>(completionHandler) {

            @Override
            public void completed(Integer result, Void attachment) {
                buffer.clear();
                completionHandler.completed(null, attachment);
            }
        });
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

}
