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

import org.ini4j.Profile.Section;

import com.github.apetrelli.scafa.http.HttpRequestManipulator;
import com.github.apetrelli.scafa.http.impl.HostPort;
import com.github.apetrelli.scafa.http.proxy.HttpConnection;
import com.github.apetrelli.scafa.http.proxy.HttpConnectionFactory;
import com.github.apetrelli.scafa.http.proxy.MappedHttpConnectionFactory;
import com.github.apetrelli.scafa.util.HttpUtils;

public class BasicAuthProxyHttpConnectionFactory implements HttpConnectionFactory {

    private String host;

    private int port;

    private String username, password;

    private HttpRequestManipulator manipulator;

    public BasicAuthProxyHttpConnectionFactory(Section section) {
        this.host = section.get("host");
        this.port = section.get("port", int.class);
        this.username = section.get("username");
        this.password = section.get("password");
        manipulator = HttpUtils.createManipulator(section);
    }

    @Override
    public HttpConnection create(MappedHttpConnectionFactory factory, AsynchronousSocketChannel sourceChannel,
            HostPort socketAddress) throws IOException {
        return new BasicAuthProxyHttpConnection(sourceChannel, factory, socketAddress, host, port, manipulator,
                username, password);
    }

}
