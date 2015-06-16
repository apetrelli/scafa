/**
 * Scafa - Universal roadwarrior non-caching proxy
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
package com.github.apetrelli.scafa.server.processor.http;

import java.nio.ByteBuffer;

import com.github.apetrelli.scafa.server.processor.Input;


public class HttpInput implements Input {
    private ByteBuffer buffer;

    private HttpBodyMode bodyMode = HttpBodyMode.EMPTY;

    private long countdown = 0L;

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

    public void setCountdown(long countdown) {
        this.countdown = countdown;
    }
    
    public void reduceCountdown(int toSubstract) {
        countdown -= toSubstract;
        if (countdown < 0L) {
            countdown = 0L;
        }
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
        caughtError = false;
    }
}
