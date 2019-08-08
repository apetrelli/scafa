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
package com.github.apetrelli.scafa.http.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

import com.github.apetrelli.scafa.http.HttpHandler;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;

public class HttpHandlerSupport implements HttpHandler {

    @Override
    public void onConnect() throws IOException {
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onResponseHeader(HttpResponse response, CompletionHandler<Void, Void> handler) {
        handler.completed(null, null);
    }

    @Override
    public void onRequestHeader(HttpRequest request, CompletionHandler<Void, Void> handler) {
        handler.completed(null, null);
    }

    @Override
    public void onBody(ByteBuffer buffer, long offset, long length, CompletionHandler<Void, Void> handler) {
        buffer.position(buffer.limit());
        handler.completed(null, null);
    }

    @Override
    public void onChunkStart(long totalOffset, long chunkLength, CompletionHandler<Void, Void> handler) {
        handler.completed(null, null);
    }

    @Override
	public void onChunk(ByteBuffer buffer, long totalOffset, long chunkOffset, long chunkLength,
			CompletionHandler<Void, Void> handler) {
        buffer.position(buffer.limit());
        handler.completed(null, null);
    }

    @Override
    public void onChunkEnd(CompletionHandler<Void, Void> handler) {
        handler.completed(null, null);
    }

    @Override
    public void onChunkedTransferEnd(CompletionHandler<Void, Void> handler) {
        handler.completed(null, null);
    }

    @Override
    public void onDataToPassAlong(ByteBuffer buffer, CompletionHandler<Void, Void> handler) {
        buffer.position(buffer.limit());
        handler.completed(null, null);
    }

    @Override
    public void onEnd() {
    }

    @Override
    public void onDisconnect() throws IOException {
    }

}
