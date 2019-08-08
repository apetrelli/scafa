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
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.proxy.HttpConnectRequest;
import com.github.apetrelli.scafa.http.proxy.ProxyHttpConnection;

public class NullProxyHttpConnection implements ProxyHttpConnection {

    private AsynchronousSocketChannel sourceChannel;

    public NullProxyHttpConnection(AsynchronousSocketChannel sourceChannel) {
        this.sourceChannel = sourceChannel;
    }

    @Override
    public void ensureConnected(CompletionHandler<Void, Void> handler) {
        handler.completed(null, null);
    }

    @Override
    public void sendHeader(HttpRequest request, CompletionHandler<Void, Void> completionHandler) {
        completionHandler.completed(null, null);
    }

    @Override
    public void connect(HttpConnectRequest request, CompletionHandler<Void, Void> completionHandler) {
        completionHandler.completed(null, null);
    }

    @Override
    public void send(ByteBuffer buffer, CompletionHandler<Void, Void> completionHandler) {
        buffer.clear();
        completionHandler.completed(null, null);
    }

    @Override
    public void end() {
        ProxyResources.getInstance().sendGenericErrorPage(sourceChannel);
    }

    @Override
    public void close() throws IOException {
        // Does nothing
    }

}
