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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ini4j.Profile.Section;

import com.github.apetrelli.scafa.http.HttpRequestManipulator;
import com.github.apetrelli.scafa.http.impl.HostPort;
import com.github.apetrelli.scafa.http.proxy.HttpConnection;
import com.github.apetrelli.scafa.http.proxy.HttpConnectionFactory;
import com.github.apetrelli.scafa.http.proxy.MappedHttpConnectionFactory;

public class AnonymousProxyHttpConnectionFactory implements HttpConnectionFactory {

    private static final Logger LOG = Logger.getLogger(AnonymousProxyHttpConnectionFactory.class.getName());

    private String host;

    private int port;

    private HttpRequestManipulator manipulator;

    public AnonymousProxyHttpConnectionFactory(Section section) {
        this.host = section.get("host");
        this.port = section.get("port", int.class);
        String className = section.get("manipulator");
        if (className != null) {
            try {
                Class<? extends HttpRequestManipulator> clazz = Class.forName(className).asSubclass(HttpRequestManipulator.class);
                manipulator = clazz.newInstance();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                LOG.log(Level.SEVERE, "Cannot instantiate manipulator: " + className, e);
            }
        }
    }

    @Override
    public HttpConnection create(MappedHttpConnectionFactory factory, AsynchronousSocketChannel sourceChannel,
            HostPort socketAddress) throws IOException {
        return new AnonymousProxyHttpConnection(sourceChannel, factory, socketAddress, host, port, manipulator);
    }

}
