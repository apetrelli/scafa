package com.github.apetrelli.scafa.http.impl;

import java.nio.ByteBuffer;

import com.github.apetrelli.scafa.http.HttpBodyMode;
import com.github.apetrelli.scafa.http.HttpProcessingContext;
import com.github.apetrelli.scafa.http.HttpSink;
import com.github.apetrelli.scafa.http.HttpStatus;
import com.github.apetrelli.scafa.proto.processor.ProtocolStateMachine;

public class HttpStateMachine<H, R> implements ProtocolStateMachine<H, HttpProcessingContext, R> {

    private static final byte CR = 13;

    private static final byte LF = 10;
    
    private HttpSink<H, R> sink;
	
	public HttpStateMachine(HttpSink<H, R> sink) {
		this.sink = sink;
	}

	@Override
	public R out(HttpProcessingContext context, H handler) {
		R retValue = null;
		while (retValue == null && context.getBuffer().hasRemaining()) {
			next(context);
			switch (context.getStatus()) {
			case IDLE:
				break;
			case START_REQUEST_LINE:
		        context.reset();
		        context.appendToLine(context.getBuffer().get());
				sink.onStart(handler);
				break;
			case REQUEST_LINE:
	            onRequestLine(context);
				break;
			case REQUEST_LINE_CR:
				endRequestLine(context);
				break;
			case REQUEST_LINE_LF:
			case HEADER_LF:
			case PENULTIMATE_BYTE:
			case LAST_BYTE:
			case CHUNK_COUNT_CR:
			case CHUNK_LF:
	            context.getBuffer().get(); // discard LF.
				break;
			case POSSIBLE_HEADER_CR:
	            context.getBuffer().get(); // discard CR.
	            context.evaluateBodyMode();
				break;
			case SEND_HEADER_AND_END:
	            context.getBuffer().get(); // discard LF.
				retValue = sink.endHeaderAndRequest(context, handler);
				break;
			case POSSIBLE_HEADER_LF:
	            context.getBuffer().get(); // discard LF.
	            retValue = sink.endHeader(context, handler);
				break;
			case HEADER:
				onHeader(context);
				break;
			case HEADER_CR:
	            context.getBuffer().get(); // discard CR
	            context.addHeaderLine();
				break;
			case BODY:
				retValue = sink.data(context, handler);
				break;
			case CHUNK_COUNT:
				onChunkCount(context);
				break;
			case CHUNK_COUNT_LF:
	            context.getBuffer().get(); // discard LF.
	            retValue = sink.endChunkCount(context, handler);
				break;
			case CHUNK:
				retValue = sink.chunkData(context, handler);
				break;
			case CHUNK_CR:
				context.getBuffer().get(); // discard CR
				retValue = sink.onChunkEnd(handler);
				break;
			case CONNECT:
				retValue = sink.onDataToPassAlong(context, handler);
			}
		}
		return retValue != null ? retValue : sink.completed();
	}
	
	private HttpStatus next(HttpProcessingContext context) {
		HttpStatus newStatus = null;
		switch (context.getStatus()) {
		case IDLE:
			newStatus = HttpStatus.START_REQUEST_LINE;
			break;
		case START_REQUEST_LINE:
			newStatus = HttpStatus.REQUEST_LINE;
			break;
		case REQUEST_LINE:
            switch (context.peekNextByte()) {
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
			newStatus = context.peekNextByte() == CR ? HttpStatus.POSSIBLE_HEADER_CR : HttpStatus.HEADER;
			break;
		case POSSIBLE_HEADER_CR:
			newStatus = context.getBodyMode() == HttpBodyMode.EMPTY ? HttpStatus.SEND_HEADER_AND_END : HttpStatus.POSSIBLE_HEADER_LF;
			break;
		case SEND_HEADER_AND_END:
			newStatus = context.isHttpConnected() ? HttpStatus.CONNECT : HttpStatus.START_REQUEST_LINE;
			break;
		case POSSIBLE_HEADER_LF:
            switch (context.getBodyMode()) {
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
            switch (context.peekNextByte()) {
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
			newStatus = context.peekNextByte() == CR ? HttpStatus.POSSIBLE_HEADER_CR : HttpStatus.HEADER;
			break;
		case BODY:
			newStatus = context.getCountdown() > 0L ? HttpStatus.BODY : HttpStatus.START_REQUEST_LINE;
			break;
		case PENULTIMATE_BYTE:
			newStatus = HttpStatus.LAST_BYTE;
			break;
		case LAST_BYTE:
			newStatus = HttpStatus.START_REQUEST_LINE;
			break;
		case CHUNK_COUNT:
            switch (context.peekNextByte()) {
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
			newStatus = context.getCountdown() > 0L ? HttpStatus.CHUNK : HttpStatus.PENULTIMATE_BYTE;
			break;
		case CHUNK:
			newStatus = context.getCountdown() > 0L ? HttpStatus.CHUNK : HttpStatus.CHUNK_CR;
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

	private void onChunkCount(HttpProcessingContext context) {
		ByteBuffer buffer = context.getBuffer();
		byte currentByte;
		currentByte = buffer.get();
		while (buffer.hasRemaining() && currentByte != CR) {
			context.appendToLine(currentByte);
		    currentByte = buffer.get();
		}
		// Evaluating of CR is not necessary since chunk count is treated when LF arrives, at the next state.
	}

	private void onHeader(HttpProcessingContext context) {
		ByteBuffer buffer = context.getBuffer();
		byte currentByte;
		currentByte = buffer.get();
		while (buffer.hasRemaining() && currentByte != CR) {
			context.appendToLine(currentByte);
		    currentByte = buffer.get();
		}
		if (currentByte == CR) {
		    context.addHeaderLine();
		}
	}

	private void onRequestLine(HttpProcessingContext context) {
		ByteBuffer buffer = context.getBuffer();
		byte currentByte;
		currentByte = buffer.get();
		while (buffer.hasRemaining() && currentByte != CR) {
		    context.appendToLine(currentByte);
		    currentByte = buffer.get();
		}
		if (currentByte == CR) {
			endRequestLine(context);
		}
	}

	private void endRequestLine(HttpProcessingContext context) {
		context.evaluateRequestLine();
	}
}
