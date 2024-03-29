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
package com.github.apetrelli.scafa.sync.http.proxy.handler;

import com.github.apetrelli.scafa.proto.processor.HandlerFactory;
import com.github.apetrelli.scafa.sync.http.HttpHandler;
import com.github.apetrelli.scafa.sync.http.gateway.GatewayHttpConnectionFactoryFactory;
import com.github.apetrelli.scafa.sync.http.proxy.ProxyHttpConnection;
import com.github.apetrelli.scafa.sync.proto.SyncSocket;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProxyHttpHandlerFactory implements HandlerFactory<HttpHandler, SyncSocket> {

    private final GatewayHttpConnectionFactoryFactory<ProxyHttpConnection> connectionFactoryFactory;

    @Override
    public HttpHandler create(SyncSocket sourceChannel) {
        return new DefaultProxyHttpHandler(connectionFactoryFactory.create(), sourceChannel);
    }

}
