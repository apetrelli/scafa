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
package com.github.apetrelli.scafa.http.ntlm;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import com.github.apetrelli.scafa.aio.DelegateCompletionHandler;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.util.HttpUtils;

public class TentativeHandler extends CapturingHandler {

    private boolean needsAuthorizing = false;

    private boolean onlyCaptureMode = false;

    private AsynchronousSocketChannel sourceChannel;

    public TentativeHandler(AsynchronousSocketChannel sourceChannel) {
        this.sourceChannel = sourceChannel;
    }

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
    public void onResponseHeader(HttpResponse response, CompletionHandler<Void, Void> completionHandler) {
        this.response = response;
        if (onlyCaptureMode || response.getCode() == 407) {
            needsAuthorizing = true;
            completionHandler.completed(null, null);
        } else {
            HttpUtils.sendHeader(response, sourceChannel, completionHandler);
        }
    }

    @Override
    public void onBody(ByteBuffer buffer, long offset, long length, CompletionHandler<Void, Void> handler) {
        if (needsAuthorizing) {
            super.onBody(buffer, offset, length, handler);
        } else {
            sourceChannel.write(buffer, null, new DelegateCompletionHandler<Integer, Void>(handler));
        }
    }

    @Override
    public void onChunkStart(long totalOffset, long chunkLength, CompletionHandler<Void, Void> handler) {
        if (needsAuthorizing) {
            super.onChunkStart(totalOffset, chunkLength, handler);
        } else {
            HttpUtils.sendChunkSize(chunkLength, sourceChannel, handler);
        }
    }

    @Override
    public void onChunk(ByteBuffer buffer, long totalOffset, long chunkOffset, long chunkLength,
            CompletionHandler<Void, Void> handler) {
        if (needsAuthorizing) {
            super.onChunk(buffer, totalOffset, chunkOffset, chunkLength, handler);
        } else {
            sourceChannel.write(buffer, null, new DelegateCompletionHandler<Integer, Void>(handler));
        }
    }

    @Override
    public void onChunkEnd(CompletionHandler<Void, Void> handler) {
        if (needsAuthorizing) {
            super.onChunkEnd(handler);
        } else {
            HttpUtils.sendNewline(sourceChannel, handler);
        }
    }

    @Override
    public void onChunkedTransferEnd(CompletionHandler<Void, Void> handler) {
        if (needsAuthorizing) {
            super.onChunkedTransferEnd(handler);
        } else {
            HttpUtils.sendNewline(sourceChannel, handler);
        }
    }
}
