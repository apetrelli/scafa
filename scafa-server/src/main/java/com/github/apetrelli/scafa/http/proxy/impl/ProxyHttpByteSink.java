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
import java.nio.channels.CompletionHandler;

import com.github.apetrelli.scafa.http.HttpHandler;
import com.github.apetrelli.scafa.http.HttpInput;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.impl.DefaultHttpByteSink;

public class ProxyHttpByteSink extends DefaultHttpByteSink<HttpHandler> {

    public ProxyHttpByteSink(AsynchronousSocketChannel sourceChannel, HttpHandler handler) {
        super(handler);
    }

    @Override
    public void data(HttpInput input, CompletionHandler<Void, Void> completionHandler) {
        if (input.isHttpConnected()) {
            handler.onDataToPassAlong(input.getBuffer(), completionHandler);
        } else {
            super.data(input, completionHandler);
        }
    }

    @Override
    protected void manageRequestHeader(HttpHandler handler, HttpInput input, HttpRequest request,
            CompletionHandler<Void, Void> completionHandler) {
        if ("CONNECT".equalsIgnoreCase(request.getMethod())) {
            input.setHttpConnected(true);
        }
        super.manageRequestHeader(handler, input, request, completionHandler);
    }
}
