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

import java.nio.ByteBuffer;

import com.github.apetrelli.scafa.processor.Input;


public class HttpInput implements Input {
    private ByteBuffer buffer;

    private HttpBodyMode bodyMode = HttpBodyMode.EMPTY;

    private long countdown = 0L;
    
    private long bodySize;
    
    private long bodyOffset;
    
    private long totalChunkedTransferLength;
    
    private long chunkOffset;
    
    private long chunkLength;

    private boolean caughtError = false;

    private boolean httpConnected = false;

    public HttpInput() {
    }

    @Override
    public byte peekNextByte() {
        return buffer.array()[buffer.position()];
    }

    public void setBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public ByteBuffer getBuffer() {
        return buffer;
    }

    public HttpBodyMode getBodyMode() {
        return bodyMode;
    }

    public void setBodyMode(HttpBodyMode bodyMode) {
        this.bodyMode = bodyMode;
    }

    public long getCountdown() {
        return countdown;
    }
    
    public long getBodySize() {
        return bodySize;
    }
    
    public void setBodySize(long bodySize) {
        this.bodySize = bodySize;
        countdown = bodySize;
        bodyOffset = 0L;
    }
    
    public long getBodyOffset() {
        return bodyOffset;
    }
    
    public long getTotalChunkedTransferLength() {
        return totalChunkedTransferLength;
    }
    
    public long getChunkOffset() {
        return chunkOffset;
    }
    
    public long getChunkLength() {
        return chunkLength;
    }
    
    public void setChunkLength(long chunkLength) {
        this.chunkLength = chunkLength;
        countdown = chunkLength;
        totalChunkedTransferLength += chunkLength;
        chunkOffset = 0L;
    }

    public void reduceBody(int toSubtract) {
        countdown -= toSubtract;
        if (countdown < 0L) {
            countdown = 0L;
        }
        bodyOffset += toSubtract;
    }
    
    public void reduceChunk(long toSubtract) {
        countdown -= toSubtract;
        if (countdown < 0L) {
            countdown = 0L;
        }
        chunkOffset += toSubtract;
    }

    public boolean isCaughtError() {
        return caughtError;
    }

    public void setCaughtError(boolean caughtError) {
        this.caughtError = caughtError;
    }

    public boolean isHttpConnected() {
        return httpConnected;
    }

    public void setHttpConnected(boolean httpConnected) {
        this.httpConnected = httpConnected;
    }

    public void reset() {
        bodyMode = HttpBodyMode.EMPTY;
        countdown = 0L;
        bodySize = 0L;
        bodyOffset = 0L;
        totalChunkedTransferLength = 0L;
        chunkOffset = 0L;
        chunkLength = 0L;
        caughtError = false;
    }
}
