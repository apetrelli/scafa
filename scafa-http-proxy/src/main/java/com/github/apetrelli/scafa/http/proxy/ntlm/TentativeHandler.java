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
package com.github.apetrelli.scafa.http.proxy.ntlm;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.proto.aio.CompletionHandlerFuture;

public class TentativeHandler extends CapturingHandler {

    private boolean needsAuthorizing = false;

    private boolean onlyCaptureMode = false;

    private HttpAsyncSocket<HttpResponse> sourceChannel;

    public TentativeHandler(HttpAsyncSocket<HttpResponse> sourceChannel) {
        this.sourceChannel = sourceChannel;
    }

    @Override
    public void reset() {
        needsAuthorizing = false;
        onlyCaptureMode = false;
        super.reset();
    }

    public boolean isNeedsAuthorizing() {
        return needsAuthorizing;
    }

    public void setOnlyCaptureMode(boolean onlyCaptureMode) {
        this.onlyCaptureMode = onlyCaptureMode;
    }
    
    @Override
    public CompletableFuture<Void> onResponseHeader(HttpResponse response) {
        this.response = response;
        if (onlyCaptureMode || response.getCode() == 407) {
            needsAuthorizing = true;
            return CompletionHandlerFuture.completeEmpty();
        } else {
            return sourceChannel.sendHeader(response);
        }
    }
    
    @Override
    public CompletableFuture<Void> onBody(ByteBuffer buffer, long offset, long length) {
        if (needsAuthorizing) {
            return super.onBody(buffer, offset, length);
        } else {
        	return sourceChannel.sendData(buffer);
        }
    }

    @Override
    public CompletableFuture<Void> onChunk(ByteBuffer buffer, long totalOffset, long chunkOffset, long chunkLength) {
        if (needsAuthorizing) {
            return super.onChunk(buffer, totalOffset, chunkOffset, chunkLength);
        } else {
        	return sourceChannel.sendData(buffer);
        }
    }
    
    @Override
    public CompletableFuture<Void> onEnd() {
    	if (needsAuthorizing) {
    		return super.onEnd();
    	} else {
    		finished = true;
    		return sourceChannel.endData();
    	}
    }
}
