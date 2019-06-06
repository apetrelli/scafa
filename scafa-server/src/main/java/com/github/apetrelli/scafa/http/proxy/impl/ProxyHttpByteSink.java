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
import java.nio.channels.CompletionHandler;

import com.github.apetrelli.scafa.http.HttpInput;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.impl.DefaultHttpByteSink;
import com.github.apetrelli.scafa.http.proxy.HttpConnectRequest;
import com.github.apetrelli.scafa.http.proxy.ProxyHttpHandler;

public class ProxyHttpByteSink extends DefaultHttpByteSink<ProxyHttpHandler> {

    public ProxyHttpByteSink(AsynchronousSocketChannel sourceChannel, ProxyHttpHandler handler) {
        super(handler);
    }

    @Override
    public void send(HttpInput input, CompletionHandler<Void, Void> completionHandler) {
        if (input.isHttpConnected()) {
            handler.onDataToPassAlong(input.getBuffer(), completionHandler);
        } else {
            super.send(input, completionHandler);
        }
    }

    @Override
    protected void manageRequestHeader(ProxyHttpHandler handler, HttpInput input, HttpRequest request,
            CompletionHandler<Void, Void> completionHandler) {
        if ("CONNECT".equalsIgnoreCase(request.getMethod())) {
            HttpConnectRequest connectRequest;
            try {
                connectRequest = new HttpConnectRequest(request);
                handler.onConnectMethod(connectRequest, completionHandler);
                input.setHttpConnected(true);
            } catch (IOException e) {
                completionHandler.failed(e, null);
            }
        } else {
            super.manageRequestHeader(handler, input, request, completionHandler);
        }
    }
}
