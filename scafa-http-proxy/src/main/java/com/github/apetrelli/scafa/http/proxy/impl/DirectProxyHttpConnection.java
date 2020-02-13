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
import java.net.URL;
import java.nio.channels.CompletionHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.proxy.HttpConnectRequest;
import com.github.apetrelli.scafa.http.proxy.MappedProxyHttpConnectionFactory;
import com.github.apetrelli.scafa.proto.aio.DelegateFailureCompletionHandler;

public class DirectProxyHttpConnection extends AbstractProxyHttpConnection<HttpAsyncSocket> {

    private static final Logger LOG = Logger.getLogger(DirectProxyHttpConnection.class.getName());

    public DirectProxyHttpConnection(MappedProxyHttpConnectionFactory factory,
            HttpAsyncSocket sourceChannel, HttpAsyncSocket socket) {
        super(factory, sourceChannel, socket, socket.getAddress());
    }

    @Override
    public void connect(HttpConnectRequest request, CompletionHandler<Void, Void> completionHandler) {
        // Already connected, need only to send a 200.
        String httpVersion = request.getHttpVersion();
        HttpResponse response = new HttpResponse(httpVersion, 200, "OK");
        sourceChannel.sendHeader(response, new DelegateFailureCompletionHandler<Void, Void>(completionHandler) {

			@Override
			public void completed(Void result, Void attachment) {
				sourceChannel.endData(completionHandler);
			}
		});
    }

    protected HttpRequest createForwardedRequest(HttpRequest request) throws IOException {
        URL realurl = new URL(request.getResource());
        HttpRequest modifiedRequest = new HttpRequest(request);
        modifiedRequest.setResource(realurl.getFile());
        if (LOG.isLoggable(Level.INFO)) {
            LOG.log(Level.INFO, "Direct connection: connected thread {0} to port {1} and URL {2}",
                    new Object[] { Thread.currentThread().getName(), socket.getAddress().toString(), request.getResource() });
        }
        return modifiedRequest;
    }

}
