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
package com.github.apetrelli.scafa.http.async;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.proto.processor.Handler;

public interface HttpHandler extends Handler {

    void onStart();

    CompletableFuture<Void> onResponseHeader(HttpResponse response);

    CompletableFuture<Void> onRequestHeader(HttpRequest request);

    CompletableFuture<Void> onBody(ByteBuffer buffer, long offset, long length);

    CompletableFuture<Void> onChunkStart(long totalOffset, long chunkLength);

    CompletableFuture<Void> onChunk(ByteBuffer buffer, long totalOffset, long chunkOffset, long chunkLength);

    CompletableFuture<Void> onChunkEnd();

    CompletableFuture<Void> onChunkedTransferEnd();

    CompletableFuture<Void> onDataToPassAlong(ByteBuffer buffer);

    CompletableFuture<Void> onEnd();
}
