package com.github.apetrelli.scafa.http.impl;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

import com.github.apetrelli.scafa.http.HttpBodyMode;
import com.github.apetrelli.scafa.http.HttpByteSink;
import com.github.apetrelli.scafa.http.HttpInput;
import com.github.apetrelli.scafa.http.HttpProcessingContext;
import com.github.apetrelli.scafa.http.HttpStatus;
import com.github.apetrelli.scafa.proto.processor.ProtocolStateMachine;
import com.github.apetrelli.scafa.proto.processor.Status;

public class HttpStateMachine implements ProtocolStateMachine<HttpInput, HttpByteSink, HttpProcessingContext> {

    private static final byte CR = 13;

    private static final byte LF = 10;

	@Override
	public Status<HttpInput, HttpByteSink> next(HttpProcessingContext context) {
		HttpStatus newStatus = null;
		switch (context.getStatus()) {
		case IDLE:
			newStatus = HttpStatus.REQUEST_LINE;
			break;
		case REQUEST_LINE:
            switch (context.getInput().peekNextByte()) {
            case CR:
                newStatus = HttpStatus.REQUEST_LINE_CR;
                break;
            case LF:
                newStatus = HttpStatus.REQUEST_LINE_LF;
                break;
            default:
                newStatus = HttpStatus.REQUEST_LINE;
            }
			break;
		case REQUEST_LINE_CR:
			newStatus = HttpStatus.REQUEST_LINE_LF;
			break;
		case REQUEST_LINE_LF:
			newStatus = context.getInput().peekNextByte() == CR ? HttpStatus.POSSIBLE_HEADER_CR : HttpStatus.HEADER;
			break;
		case POSSIBLE_HEADER:
            switch (context.getInput().peekNextByte()) {
            case CR:
                newStatus = HttpStatus.POSSIBLE_HEADER_CR;
                break;
            case LF:
                newStatus = HttpStatus.POSSIBLE_HEADER_LF;
                break;
            default:
                newStatus = HttpStatus.HEADER;
            }
			break;
		case POSSIBLE_HEADER_CR:
			newStatus = context.getInput().getBodyMode() == HttpBodyMode.EMPTY ? HttpStatus.SEND_HEADER_AND_END : HttpStatus.POSSIBLE_HEADER_LF;
			break;
		case SEND_HEADER_AND_END:
			newStatus = context.getInput().isHttpConnected() ? HttpStatus.CONNECT : HttpStatus.IDLE;
			break;
		case POSSIBLE_HEADER_LF:
            switch (context.getInput().getBodyMode()) {
            case EMPTY:
                newStatus = HttpStatus.IDLE;
                break;
            case BODY:
                newStatus = HttpStatus.BODY;
                break;
            case CHUNKED:
                newStatus = HttpStatus.CHUNK_COUNT;
            }
			break;
		case HEADER:
            switch (context.getInput().peekNextByte()) {
            case CR:
                newStatus = HttpStatus.HEADER_CR;
                break;
            case LF:
                newStatus = HttpStatus.HEADER_LF;
                break;
            default:
                newStatus = HttpStatus.HEADER;
            }
			break;
		case HEADER_CR:
			newStatus = HttpStatus.HEADER_LF;
			break;
		case HEADER_LF:
			newStatus = context.getInput().peekNextByte() == CR ? HttpStatus.POSSIBLE_HEADER_CR : HttpStatus.HEADER;
			break;
		case BODY:
			newStatus = context.getInput().getCountdown() > 0L ? HttpStatus.BODY : HttpStatus.IDLE;
			break;
		case PENULTIMATE_BYTE:
			newStatus = HttpStatus.LAST_BYTE;
			break;
		case LAST_BYTE:
			newStatus = HttpStatus.IDLE;
			break;
		case CHUNK_COUNT:
            switch (context.getInput().peekNextByte()) {
            case CR:
                newStatus = HttpStatus.CHUNK_COUNT_CR;
                break;
            case LF:
                newStatus = HttpStatus.CHUNK_COUNT_LF;
                break;
            default:
                newStatus = HttpStatus.CHUNK_COUNT;
            }
			break;
		case CHUNK_COUNT_CR:
			newStatus = HttpStatus.CHUNK_COUNT_LF;
			break;
		case CHUNK_COUNT_LF:
			newStatus = context.getInput().getCountdown() > 0L ? HttpStatus.CHUNK : HttpStatus.PENULTIMATE_BYTE;
			break;
		case CHUNK:
			newStatus = context.getInput().getCountdown() > 0L ? HttpStatus.CHUNK : HttpStatus.CHUNK_CR;
			break;
		case CHUNK_CR:
			newStatus = HttpStatus.CHUNK_LF;
			break;
		case CHUNK_LF:
			newStatus = HttpStatus.CHUNK_COUNT;
			break;
		case CONNECT:
			newStatus = HttpStatus.CONNECT;
		}
		context.setStatus(newStatus);
		return newStatus;
	}

	@Override
	public void out(HttpProcessingContext context, HttpByteSink sink, CompletionHandler<Void, Void> completionHandler) {
		HttpInput input = context.getInput();
		switch (context.getStatus()) {
		case IDLE:
            sink.start(input);
            completionHandler.completed(null, null);
			break;
		case REQUEST_LINE:
            onRequestLine(sink, completionHandler, input);
			break;
		case REQUEST_LINE_CR:
			sink.endRequestLine(completionHandler);
			break;
		case REQUEST_LINE_LF:
            sink.beforeHeader(input.getBuffer().get());
            completionHandler.completed(null, null);
			break;
		case POSSIBLE_HEADER:
            onPossibleHeader(sink, completionHandler, input);
			break;
		case POSSIBLE_HEADER_CR:
            input.getBuffer().get(); // discard CR.
            sink.preEndHeader(input);
            completionHandler.completed(null, null);
			break;
		case SEND_HEADER_AND_END:
            input.getBuffer().get(); // discard LF.
            sink.endHeaderAndRequest(input, completionHandler);
			break;
		case POSSIBLE_HEADER_LF:
            input.getBuffer().get(); // discard LF.
            sink.endHeader(input, completionHandler);
			break;
		case HEADER:
            onHeader(sink, completionHandler, input);
			break;
		case HEADER_CR:
            input.getBuffer().get(); // discard CR
            sink.endHeaderLine();
            completionHandler.completed(null, null);
			break;
		case HEADER_LF:
            sink.beforeHeaderLine(input.getBuffer().get());
            completionHandler.completed(null, null);
			break;
		case BODY:
            sink.data(input, completionHandler);
			break;
		case PENULTIMATE_BYTE:
            sink.chunkedTransferLastCr(input.getBuffer().get());
            completionHandler.completed(null, null);
			break;
		case LAST_BYTE:
            sink.chunkedTransferLastLf(input.getBuffer().get());
            completionHandler.completed(null, null);
			break;
		case CHUNK_COUNT:
            onChunkCount(sink, completionHandler, input);
			break;
		case CHUNK_COUNT_CR:
            input.getBuffer().get(); // discard CR.
            sink.preEndChunkCount();
            completionHandler.completed(null, null);
			break;
		case CHUNK_COUNT_LF:
            input.getBuffer().get(); // discard LF.
            sink.endChunkCount(input, completionHandler);
			break;
		case CHUNK:
            sink.chunkData(input, completionHandler);
			break;
		case CHUNK_CR:
            sink.afterEndOfChunk(input.getBuffer().get(), completionHandler);
			break;
		case CHUNK_LF:
            sink.beforeChunkCount(input.getBuffer().get());
            completionHandler.completed(null, null);
			break;
		case CONNECT:
            sink.data(input, completionHandler);
		}
	}

	private void onChunkCount(HttpByteSink sink, CompletionHandler<Void, Void> completionHandler, HttpInput input) {
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

	private void onHeader(HttpByteSink sink, CompletionHandler<Void, Void> completionHandler, HttpInput input) {
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

	private void onPossibleHeader(HttpByteSink sink, CompletionHandler<Void, Void> completionHandler, HttpInput input) {
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

	private void onRequestLine(HttpByteSink sink, CompletionHandler<Void, Void> completionHandler, HttpInput input) {
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
}
