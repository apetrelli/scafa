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

import java.nio.channels.CompletionHandler;

import com.github.apetrelli.scafa.processor.ByteSink;

public interface HttpByteSink extends ByteSink<HttpInput>{

    void appendRequestLine(byte currentByte);

    void endRequestLine(HttpInput input, CompletionHandler<Void, Void> completionHandler);

    void beforeHeader(byte currentByte);

    void beforeHeaderLine(byte currentByte);

    void appendHeader(byte currentByte);

    void endHeaderLine(byte currentByte);

    void preEndHeader(HttpInput input);

    void endHeader(HttpInput input, CompletionHandler<Void, Void> completionHandler);

    void endHeaderAndRequest(HttpInput input, CompletionHandler<Void, Void> completionHandler);

    void afterEndOfChunk(byte currentByte, CompletionHandler<Void, Void> completionHandler);

    void beforeChunkCount(byte currentByte);

    void appendChunkCount(byte currentByte);

    void preEndChunkCount(byte currentByte);

    void endChunkCount(HttpInput input, CompletionHandler<Void, Void> completionHandler);

    void chunkedTransferLastCr(byte currentByte);

    void chunkedTransferLastLf(byte currentByte);
}
