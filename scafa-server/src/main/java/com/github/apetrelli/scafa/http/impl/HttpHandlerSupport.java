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
import java.util.List;
import java.util.Map;

import com.github.apetrelli.scafa.http.HttpHandler;

public class HttpHandlerSupport implements HttpHandler {

    @Override
    public void onConnect() throws IOException {
    }

    @Override
    public void onStart() throws IOException {
    }

    @Override
    public void onResponseHeader(String httpVersion, int responseCode, String responseMessage,
            Map<String, List<String>> headers) throws IOException {
    }

    @Override
    public void onRequestHeader(String method, String url, String httpVersion, Map<String, List<String>> headers)
            throws IOException {
    }

    @Override
    public void onBody(ByteBuffer buffer, long offset, long length) throws IOException {
        buffer.position(buffer.limit());
    }

    @Override
    public void onChunkStart(long totalOffset, long chunkLength) throws IOException {
    }

    @Override
    public void onChunk(byte[] buffer, int position, int length, long totalOffset, long chunkOffset, long chunkLength)
            throws IOException {
    }

    @Override
    public void onChunkEnd() throws IOException {
    }

    @Override
    public void onChunkedTransferEnd() throws IOException {
    }

    @Override
    public void onEnd() throws IOException {
    }

    @Override
    public void onDisconnect() throws IOException {
    }

}
