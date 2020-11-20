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
package com.github.apetrelli.scafa.http.sync.impl;

import java.nio.ByteBuffer;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.sync.HttpHandler;
import com.github.apetrelli.scafa.proto.processor.HandlerSupport;

public class HttpHandlerSupport extends HandlerSupport implements HttpHandler {

    @Override
    public void onStart() {
    	// Does nothing.
    }

    @Override
    public void onResponseHeader(HttpResponse response) {
    	// Does nothing.
    }
    
    @Override
    public void onRequestHeader(HttpRequest request) {
    	// Does nothing.
    }
    
    @Override
    public void onBody(ByteBuffer buffer, long offset, long length) {
        buffer.position(buffer.limit());
    }
    
    @Override
    public void onChunkStart(long totalOffset, long chunkLength) {
    	// Does nothing.
    }
    
    @Override
    public void onChunk(ByteBuffer buffer, long totalOffset,
    		long chunkOffset, long chunkLength) {
        buffer.position(buffer.limit());
    }
    
    @Override
    public void onChunkEnd() {
    	// Does nothing.
    }
    
    @Override
    public void onChunkedTransferEnd() {
    	// Does nothing.
    }
    
    @Override
    public void onDataToPassAlong(ByteBuffer buffer) {
        buffer.position(buffer.limit());
    }
    
    @Override
    public void onEnd() {
    	// Does nothing.
    }
}
