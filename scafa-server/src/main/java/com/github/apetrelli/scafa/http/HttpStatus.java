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
import java.nio.channels.CompletionHandler;

import com.github.apetrelli.scafa.proto.processor.Status;


public enum HttpStatus implements Status<HttpInput, HttpByteSink> {

    IDLE() {

        @Override
        public HttpStatus next(HttpInput input) {
            return REQUEST_LINE;
        }

        @Override
        public void out(HttpInput input, HttpByteSink sink, CompletionHandler<Void, Void> completionHandler) {
            sink.start(input);
            completionHandler.completed(null, null);
        }
    },
    REQUEST_LINE() {

        @Override
        public HttpStatus next(HttpInput input) {
            HttpStatus newStatus;
            switch (input.peekNextByte()) {
            case CR:
                newStatus = REQUEST_LINE_CR;
                break;
            case LF:
                newStatus = REQUEST_LINE_LF;
                break;
            default:
                newStatus = REQUEST_LINE;
            }
            return newStatus;
        }

        @Override
        public void out(HttpInput input, HttpByteSink sink, CompletionHandler<Void, Void> completionHandler) {
            ByteBuffer buffer = input.getBuffer();
            byte currentByte;
            currentByte = buffer.get();
            while (buffer.hasRemaining() && currentByte != CR) {
                sink.appendRequestLine(currentByte);
                currentByte = buffer.get();
            }
            if (currentByte == CR) {
                sink.endRequestLine(completionHandler);
            } else {
                completionHandler.completed(null, null);
            }
        }

    },
    REQUEST_LINE_CR() {

        @Override
        public HttpStatus next(HttpInput input) {
            return REQUEST_LINE_LF;
        }

        @Override
        public void out(HttpInput input, HttpByteSink sink, CompletionHandler<Void, Void> completionHandler) {
            sink.endRequestLine(completionHandler);
        }

    },
    REQUEST_LINE_LF() {

        @Override
        public HttpStatus next(HttpInput input) {
            return input.peekNextByte() == CR ? POSSIBLE_HEADER_CR : HEADER;
        }

        @Override
        public void out(HttpInput input, HttpByteSink sink, CompletionHandler<Void, Void> completionHandler) {
            sink.beforeHeader(input.getBuffer().get());
            completionHandler.completed(null, null);
        }

    },
    POSSIBLE_HEADER() {

        @Override
        public HttpStatus next(HttpInput input) {
            HttpStatus newStatus;
            switch (input.peekNextByte()) {
            case CR:
                newStatus = POSSIBLE_HEADER_CR;
                break;
            case LF:
                newStatus = POSSIBLE_HEADER_LF;
                break;
            default:
                newStatus = HEADER;
            }
            return newStatus;
        }

        @Override
        public void out(HttpInput input, HttpByteSink sink, CompletionHandler<Void, Void> completionHandler) {
            ByteBuffer buffer = input.getBuffer();
            byte currentByte;
            currentByte = buffer.get();
            while (buffer.hasRemaining() && currentByte != CR) {
                sink.appendHeader(currentByte);
                currentByte = buffer.get();
            }
            if (currentByte == CR) {
                sink.preEndHeader(input);
            }
            completionHandler.completed(null, null);
        }

    },
    POSSIBLE_HEADER_CR() {

        @Override
        public HttpStatus next(HttpInput input) {
            return input.getBodyMode() == HttpBodyMode.EMPTY ? SEND_HEADER_AND_END : POSSIBLE_HEADER_LF;
        }

        @Override
        public void out(HttpInput input, HttpByteSink sink, CompletionHandler<Void, Void> completionHandler) {
            input.getBuffer().get(); // discard CR.
            sink.preEndHeader(input);
            completionHandler.completed(null, null);
        }

    },
    SEND_HEADER_AND_END() {

        @Override
        public HttpStatus next(HttpInput input) {
            return input.isHttpConnected() ? CONNECT : IDLE;
        }

        @Override
        public void out(HttpInput input, HttpByteSink sink, CompletionHandler<Void, Void> completionHandler) {
            input.getBuffer().get(); // discard LF.
            sink.endHeaderAndRequest(input, completionHandler);
        }

    },
    POSSIBLE_HEADER_LF() {

        @Override
        public HttpStatus next(HttpInput input) {
            HttpStatus retValue = null;
            switch (input.getBodyMode()) {
            case EMPTY:
                retValue = IDLE;
                break;
            case BODY:
                retValue = HttpStatus.BODY;
                break;
            case CHUNKED:
                retValue = HttpStatus.CHUNK_COUNT;
            }
            return retValue;
        }

        @Override
        public void out(HttpInput input, HttpByteSink sink, CompletionHandler<Void, Void> completionHandler) {
            input.getBuffer().get(); // discard LF.
            sink.endHeader(input, completionHandler);
        }

    },
    HEADER() {

        @Override
        public HttpStatus next(HttpInput input) {
            HttpStatus newStatus;
            switch (input.peekNextByte()) {
            case CR:
                newStatus = HEADER_CR;
                break;
            case LF:
                newStatus = HEADER_LF;
                break;
            default:
                newStatus = HEADER;
            }
            return newStatus;
        }

        @Override
        public void out(HttpInput input, HttpByteSink sink, CompletionHandler<Void, Void> completionHandler) {
            ByteBuffer buffer = input.getBuffer();
            byte currentByte;
            currentByte = buffer.get();
            while (buffer.hasRemaining() && currentByte != CR) {
                sink.appendHeader(currentByte);
                currentByte = buffer.get();
            }
            if (currentByte == CR) {
                sink.endHeaderLine();
            }
            completionHandler.completed(null, null);
        }

    },
    HEADER_CR() {

        @Override
        public HttpStatus next(HttpInput input) {
            return HEADER_LF;
        }

        @Override
        public void out(HttpInput input, HttpByteSink sink, CompletionHandler<Void, Void> completionHandler) {
            input.getBuffer().get(); // discard CR
            sink.endHeaderLine();
            completionHandler.completed(null, null);
        }

    },
    HEADER_LF() {

        @Override
        public HttpStatus next(HttpInput input) {
            return input.peekNextByte() == CR ? POSSIBLE_HEADER_CR : HEADER;
        }

        @Override
        public void out(HttpInput input, HttpByteSink sink, CompletionHandler<Void, Void> completionHandler) {
            sink.beforeHeaderLine(input.getBuffer().get());
            completionHandler.completed(null, null);
        }

    },
    BODY() {

        @Override
        public HttpStatus next(HttpInput input) {
            return input.getCountdown() > 0L ? BODY : IDLE;
        }

        @Override
        public void out(HttpInput input, HttpByteSink sink, CompletionHandler<Void, Void> completionHandler) {
            sink.send(input, completionHandler);
        }

    },
    PENULTIMATE_BYTE() {

        @Override
        public HttpStatus next(HttpInput input) {
            return LAST_BYTE;
        }

        @Override
        public void out(HttpInput input, HttpByteSink sink, CompletionHandler<Void, Void> completionHandler) {
            sink.chunkedTransferLastCr(input.getBuffer().get());
            completionHandler.completed(null, null);
        }

    },
    LAST_BYTE() {

        @Override
        public HttpStatus next(HttpInput input) {
            return IDLE;
        }

        @Override
        public void out(HttpInput input, HttpByteSink sink, CompletionHandler<Void, Void> completionHandler) {
            sink.chunkedTransferLastLf(input.getBuffer().get());
            completionHandler.completed(null, null);
        }

    },
    CHUNK_COUNT() {

        @Override
        public HttpStatus next(HttpInput input) {
            HttpStatus newStatus;
            switch (input.peekNextByte()) {
            case CR:
                newStatus = CHUNK_COUNT_CR;
                break;
            case LF:
                newStatus = CHUNK_COUNT_LF;
                break;
            default:
                newStatus = CHUNK_COUNT;
            }
            return newStatus;
        }

        @Override
        public void out(HttpInput input, HttpByteSink sink, CompletionHandler<Void, Void> completionHandler) {
            ByteBuffer buffer = input.getBuffer();
            byte currentByte;
            currentByte = buffer.get();
            while (buffer.hasRemaining() && currentByte != CR) {
                sink.appendChunkCount(input.getBuffer().get());
                currentByte = buffer.get();
            }
            if (currentByte == CR) {
                sink.preEndChunkCount();
            }
            completionHandler.completed(null, null);
        }
    },
    CHUNK_COUNT_CR() {

        @Override
        public HttpStatus next(HttpInput input) {
            return CHUNK_COUNT_LF;
        }

        @Override
        public void out(HttpInput input, HttpByteSink sink, CompletionHandler<Void, Void> completionHandler) {
            input.getBuffer().get(); // discard CR.
            sink.preEndChunkCount();
            completionHandler.completed(null, null);
        }

    },
    CHUNK_COUNT_LF() {

        @Override
        public HttpStatus next(HttpInput input) {
            return input.getCountdown() > 0L ? CHUNK : PENULTIMATE_BYTE;
        }

        @Override
        public void out(HttpInput input, HttpByteSink sink, CompletionHandler<Void, Void> completionHandler) {
            input.getBuffer().get(); // discard LF.
            sink.endChunkCount(input, completionHandler);
        }

    },
    CHUNK() {

        @Override
        public HttpStatus next(HttpInput input) {
            return input.getCountdown() > 0L ? CHUNK : CHUNK_CR;
        }

        @Override
        public void out(HttpInput input, HttpByteSink sink, CompletionHandler<Void, Void> completionHandler) {
            sink.sendChunkData(input, completionHandler);
        }

    },
    CHUNK_CR() {

        @Override
        public HttpStatus next(HttpInput input) {
            return CHUNK_LF;
        }

        @Override
        public void out(HttpInput input, HttpByteSink sink, CompletionHandler<Void, Void> completionHandler) {
            sink.afterEndOfChunk(input.getBuffer().get(), completionHandler);
        }

    },
    CHUNK_LF() {

        @Override
        public HttpStatus next(HttpInput input) {
            return CHUNK_COUNT;
        }

        @Override
        public void out(HttpInput input, HttpByteSink sink, CompletionHandler<Void, Void> completionHandler) {
            sink.beforeChunkCount(input.getBuffer().get());
            completionHandler.completed(null, null);
        }

    },
    CONNECT() {

        @Override
        public Status<HttpInput, HttpByteSink> next(HttpInput input) {
            return CONNECT;
        }

        @Override
        public void out(HttpInput input, HttpByteSink sink, CompletionHandler<Void, Void> completionHandler) {
            sink.send(input, completionHandler);
        }

    };

    private static final byte CR = 13;

    private static final byte LF = 10;
}
