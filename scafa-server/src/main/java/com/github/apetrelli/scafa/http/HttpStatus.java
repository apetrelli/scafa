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
import java.nio.channels.CompletionHandler;

import com.github.apetrelli.scafa.server.Status;


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
            return input.peekNextByte() == CR ? REQUEST_LINE_CR : REQUEST_LINE;
        }

        @Override
        public void out(HttpInput input, HttpByteSink sink, CompletionHandler<Void, Void> completionHandler) {
            sink.appendRequestLine(input.getBuffer().get());
            completionHandler.completed(null, null);
        }

    },
    REQUEST_LINE_CR() {

        @Override
        public HttpStatus next(HttpInput input) {
            return REQUEST_LINE_LF;
        }

        @Override
        public void out(HttpInput input, HttpByteSink sink, CompletionHandler<Void, Void> completionHandler) {
            sink.endRequestLine(input);
            completionHandler.completed(null, null);
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
            return input.peekNextByte() == CR ? POSSIBLE_HEADER_CR : HEADER;
        }

        @Override
        public void out(HttpInput input, HttpByteSink sink, CompletionHandler<Void, Void> completionHandler) {
            sink.appendHeader(input.getBuffer().get());
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
            sink.endHeader(input, completionHandler);
        }

    },
    HEADER() {

        @Override
        public HttpStatus next(HttpInput input) {
            return input.peekNextByte() == CR ? HEADER_CR : HEADER;
        }

        @Override
        public void out(HttpInput input, HttpByteSink sink, CompletionHandler<Void, Void> completionHandler) {
            sink.appendHeader(input.getBuffer().get());
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
            sink.endHeaderLine(input.getBuffer().get());
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
            try {
				sink.send(input);
	            completionHandler.completed(null, null);
			} catch (IOException e) {
				completionHandler.failed(e, null);
			}
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
            return input.peekNextByte() == CR ? CHUNK_COUNT_CR : CHUNK_COUNT;
        }

        @Override
        public void out(HttpInput input, HttpByteSink sink, CompletionHandler<Void, Void> completionHandler) {
            sink.appendChunkCount(input.getBuffer().get());
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
            sink.preEndChunkCount(input.getBuffer().get());
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
            try {
                sink.endChunkCount(input);
	            completionHandler.completed(null, null);
			} catch (IOException e) {
				completionHandler.failed(e, null);
			}
        }

    },
    CHUNK() {

        @Override
        public HttpStatus next(HttpInput input) {
            return input.getCountdown() > 0L ? CHUNK : CHUNK_CR;
        }

        @Override
        public void out(HttpInput input, HttpByteSink sink, CompletionHandler<Void, Void> completionHandler) {
            try {
                sink.sendChunkData(input);
	            completionHandler.completed(null, null);
			} catch (IOException e) {
				completionHandler.failed(e, null);
			}
        }

    },
    CHUNK_CR() {

        @Override
        public HttpStatus next(HttpInput input) {
            return CHUNK_LF;
        }

        @Override
        public void out(HttpInput input, HttpByteSink sink, CompletionHandler<Void, Void> completionHandler) {
            try {
                sink.afterEndOfChunk(input.getBuffer().get());
	            completionHandler.completed(null, null);
			} catch (IOException e) {
				completionHandler.failed(e, null);
			}
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
            try {
                sink.send(input);
	            completionHandler.completed(null, null);
			} catch (IOException e) {
				completionHandler.failed(e, null);
			}
        }

    };

    private static final byte CR = 13; // LF is implicit.
}
