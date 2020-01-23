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

import java.nio.channels.AsynchronousSocketChannel;

import com.github.apetrelli.scafa.http.proxy.HttpRequestManipulator;
import com.github.apetrelli.scafa.http.proxy.MappedProxyHttpConnectionFactory;
import com.github.apetrelli.scafa.http.proxy.ProxyHttpConnection;
import com.github.apetrelli.scafa.http.proxy.ProxyHttpConnectionFactory;
import com.github.apetrelli.scafa.proto.client.HostPort;

public class AnonymousProxyHttpConnectionFactory implements ProxyHttpConnectionFactory {

    private HostPort proxySocketAddress;

    private String interfaceName;

    private boolean forceIpV4;

    private HttpRequestManipulator manipulator;
    
    public AnonymousProxyHttpConnectionFactory(HostPort proxySocketAddress, String interfaceName, boolean forceIpV4,
            HttpRequestManipulator manipulator) {
        this.proxySocketAddress = proxySocketAddress;
        this.interfaceName = interfaceName;
        this.forceIpV4 = forceIpV4;
        this.manipulator = manipulator;
    }

    @Override
    public ProxyHttpConnection create(MappedProxyHttpConnectionFactory factory, AsynchronousSocketChannel sourceChannel,
            HostPort socketAddress) {
        return new AnonymousProxyHttpConnection(factory, sourceChannel, proxySocketAddress, socketAddress, interfaceName,
                forceIpV4, manipulator);
    }

}