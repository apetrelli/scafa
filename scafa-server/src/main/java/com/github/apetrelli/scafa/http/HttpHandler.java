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
package com.github.apetrelli.scafa.http;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

public interface HttpHandler {

    void onConnect() throws IOException;

    void onStart() throws IOException;

    void onResponseHeader(HttpResponse response) throws IOException;

    void onRequestHeader(HttpRequest request, CompletionHandler<Void, Void> handler);

    void onBody(ByteBuffer buffer, long offset, long length) throws IOException;

    void onChunkStart(long totalOffset, long chunkLength) throws IOException;

    void onChunk(byte[] buffer, int position, int length, long totalOffset, long chunkOffset, long chunkLength)
            throws IOException;

    void onChunkEnd() throws IOException;

    void onChunkedTransferEnd() throws IOException;

    void onEnd() throws IOException;

    void onDisconnect() throws IOException;
}
