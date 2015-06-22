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
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import com.github.apetrelli.scafa.http.HttpConnectionFactory;
import com.github.apetrelli.scafa.util.HttpUtils;

public class DirectHttpConnection extends AbstractHttpConnection {

    public DirectHttpConnection(HttpConnectionFactory factory,
            AsynchronousSocketChannel sourceChannel, HostPort socketAddress)
            throws IOException {
        super(factory, sourceChannel);
        HttpUtils.getFuture(channel.connect(new InetSocketAddress(socketAddress.getHost(), socketAddress.getPort())));
        prepareChannel(factory, sourceChannel, socketAddress);
    }

    @Override
    public void sendHeader(String method, String url,
            String httpVersion, Map<String, List<String>> headers) throws IOException {
        URL realurl = new URL(url);
        String requestLine = method + " " + realurl.getFile() + " " + httpVersion;
        HttpUtils.sendHeader(requestLine, headers, buffer, channel);
    }

    @Override
    public void connect(String method, String host, int port, String httpVersion, Map<String, List<String>> headers)
            throws IOException {
        Charset charset = StandardCharsets.US_ASCII;
        // Already connected, need only to send a 200.
        buffer.put(httpVersion.getBytes(charset)).put(SPACE).put("200".getBytes(charset)).put(SPACE)
                .put("OK".getBytes(charset)).put(CR).put(LF).put(CR).put(LF);
        buffer.flip();
        HttpUtils.getFuture(sourceChannel.write(buffer));
        buffer.clear();
    }

}
