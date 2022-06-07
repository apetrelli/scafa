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
package com.github.apetrelli.scafa.http.proxy.sync.ntlm;

import static com.github.apetrelli.scafa.http.HttpCodes.PROXY_AUTHENTICATION_REQUIRED;

import java.nio.ByteBuffer;

import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.sync.http.HttpSyncSocket;

public class TentativeHandler extends CapturingHandler {

    private boolean needsAuthorizing = false;

    private boolean onlyCaptureMode = false;

    private HttpSyncSocket<HttpResponse> sourceChannel;
    
    private ByteBuffer writeBuffer;

    public TentativeHandler(HttpSyncSocket<HttpResponse> sourceChannel) {
        this.sourceChannel = sourceChannel;
    }
    
    public void setWriteBuffer(ByteBuffer writeBuffer) {
		this.writeBuffer = writeBuffer;
	}

    @Override
    public void reset() {
        needsAuthorizing = false;
        onlyCaptureMode = false;
        writeBuffer = null;
        super.reset();
    }

    public boolean isNeedsAuthorizing() {
        return needsAuthorizing;
    }

    public void setOnlyCaptureMode(boolean onlyCaptureMode) {
        this.onlyCaptureMode = onlyCaptureMode;
    }
    
    @Override
    public void onResponseHeader(HttpResponse response) {
        this.response = response;
        if (onlyCaptureMode || PROXY_AUTHENTICATION_REQUIRED.equals(response.getCode())) {
            needsAuthorizing = true;
        } else {
            sourceChannel.sendHeader(response, writeBuffer);
        }
    }
    
    @Override
    public void onBody(ByteBuffer buffer, long offset, long length) {
        if (needsAuthorizing) {
            super.onBody(buffer, offset, length);
        } else {
        	sourceChannel.sendData(buffer);
        }
    }

    @Override
    public void onChunk(ByteBuffer buffer, long totalOffset, long chunkOffset, long chunkLength) {
        if (needsAuthorizing) {
            super.onChunk(buffer, totalOffset, chunkOffset, chunkLength);
        } else {
        	sourceChannel.sendData(buffer);
        }
    }
    
    @Override
    public void onEnd() {
    	if (needsAuthorizing) {
    		super.onEnd();
    	} else {
    		finished = true;
    		sourceChannel.endData();
    	}
    }
}
