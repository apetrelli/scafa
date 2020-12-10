package com.github.apetrelli.scafa.http.impl;

import java.nio.ByteBuffer;
import java.util.Arrays;

import com.github.apetrelli.scafa.http.HttpBodyMode;
import com.github.apetrelli.scafa.http.HttpException;
import com.github.apetrelli.scafa.http.HttpProcessingContext;
import com.github.apetrelli.scafa.http.HttpSink;
import com.github.apetrelli.scafa.http.HttpStatus;
import com.github.apetrelli.scafa.proto.processor.ProtocolStateMachine;

public class HttpStateMachine<H, R> implements ProtocolStateMachine<H, HttpProcessingContext, R> {

    private static final byte CR = 13;

    private static final byte LF = 10;
    
    private static final byte SPACE = 32;
    
    private static final byte COLON = 58;
    
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
				sink.onStart(handler);
				break;
			case REQUEST_LINE_FIRST_TOKEN:
	            onRequestLine(context);
				break;
			case REQUEST_LINE_SECOND_TOKEN:
				onRequestSecondToken(context, SPACE);
				break;
			case REQUEST_LINE_FINAL_CONTENT:
				onRequestFinalContent(context, SPACE);
				break;
			case REQUEST_LINE_CR:
	            context.getBuffer().get(); // discard CR.
				context.evaluateFinalContent(0, context.getBuffer().position());
				endRequestLine(context);
				break;
			case REQUEST_LINE_LF:
			case HEADER_LF:
			case PENULTIMATE_BYTE:
			case LAST_BYTE:
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
			case HEADER_NAME:
				onHeader(context);
				break;
			case HEADER_VALUE:
				onHeaderValue(context, SPACE);
				break;
			case HEADER_CR:
	            context.getBuffer().get(); // discard CR
				context.evaluateHeaderValue(0, context.getBuffer().position());
	            context.addHeaderLine();
				break;
			case BODY:
				retValue = sink.data(context, handler);
				break;
			case CHUNK_COUNT:
				onChunkCount(context);
				break;
			case CHUNK_COUNT_CR:
	            context.getBuffer().get(); // discard CR
				context.evaluateChunkLength(0, context.getBuffer().position());
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
			newStatus = HttpStatus.REQUEST_LINE_FIRST_TOKEN;
			break;
		case REQUEST_LINE_FIRST_TOKEN:
            switch (context.peekNextByte()) {
            case CR:
                newStatus = HttpStatus.REQUEST_LINE_CR;
                break;
            case LF:
                newStatus = HttpStatus.REQUEST_LINE_LF;
                break;
            case SPACE:
            	newStatus = HttpStatus.REQUEST_LINE_SECOND_TOKEN;
            	break;
            default:
                newStatus = HttpStatus.REQUEST_LINE_FIRST_TOKEN;
            }
			break;
		case REQUEST_LINE_SECOND_TOKEN:
            switch (context.peekNextByte()) {
            case CR:
                newStatus = HttpStatus.REQUEST_LINE_CR;
                break;
            case LF:
                newStatus = HttpStatus.REQUEST_LINE_LF;
                break;
            case SPACE:
            	newStatus = HttpStatus.REQUEST_LINE_FINAL_CONTENT;
            	break;
            default:
                newStatus = HttpStatus.REQUEST_LINE_SECOND_TOKEN;
            }
			break;
		case REQUEST_LINE_FINAL_CONTENT:
            switch (context.peekNextByte()) {
            case CR:
                newStatus = HttpStatus.REQUEST_LINE_CR;
                break;
            case LF:
                newStatus = HttpStatus.REQUEST_LINE_LF;
                break;
            default:
                newStatus = HttpStatus.REQUEST_LINE_FINAL_CONTENT;
            }
			break;
		case REQUEST_LINE_CR:
			newStatus = HttpStatus.REQUEST_LINE_LF;
			break;
		case REQUEST_LINE_LF:
			newStatus = context.peekNextByte() == CR ? HttpStatus.POSSIBLE_HEADER_CR : HttpStatus.HEADER_NAME;
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
		case HEADER_NAME:
            switch (context.peekNextByte()) {
            case CR:
                newStatus = HttpStatus.HEADER_CR;
                break;
            case LF:
                newStatus = HttpStatus.HEADER_LF;
                break;
            case SPACE:
            	newStatus = HttpStatus.HEADER_VALUE;
            	break;
            default:
                newStatus = HttpStatus.HEADER_NAME;
            }
			break;
		case HEADER_VALUE:
            switch (context.peekNextByte()) {
            case CR:
                newStatus = HttpStatus.HEADER_CR;
                break;
            case LF:
                newStatus = HttpStatus.HEADER_LF;
                break;
            default:
                newStatus = HttpStatus.HEADER_VALUE;
            }
			break;
		case HEADER_CR:
			newStatus = HttpStatus.HEADER_LF;
			break;
		case HEADER_LF:
			newStatus = context.peekNextByte() == CR ? HttpStatus.POSSIBLE_HEADER_CR : HttpStatus.HEADER_NAME;
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
		int from = buffer.position();
		byte currentByte = skipContent(buffer, (byte) 0);
		if (currentByte == CR) {
			context.evaluateChunkLength(from, buffer.position() - 1);
		} else {
			setCarry(context, from);
		}
	}

	private void onHeader(HttpProcessingContext context) {
		ByteBuffer buffer = context.getBuffer();
		int from = buffer.position();
		byte currentByte = 0;
		while (buffer.hasRemaining() && currentByte != COLON && currentByte != CR && currentByte != SPACE) {
		    currentByte = buffer.get();
		}
		if (currentByte == COLON) {
			context.evaluateHeaderName(from, buffer.position() - 1);
			context.setStatus(HttpStatus.HEADER_VALUE);
			if (buffer.hasRemaining()) {
				currentByte = buffer.get(); // Go to next whitespace, or real header value.
				onHeaderValue(context, currentByte);
			}
		} else if (currentByte == CR || currentByte == SPACE) {
			throw new HttpException("The header line is invalid");
		} else {
			setCarry(context, from);
		}
	}

	private void onHeaderValue(HttpProcessingContext context, byte currentByte) {
		ByteBuffer buffer = context.getBuffer();
		currentByte = skipWhitespace(buffer, currentByte);
		if (currentByte == CR) {
			throw new HttpException("The header line ended incorrectly after first token");
		} else if (currentByte != SPACE) {
			int from = buffer.position() - 1; // The first byte has been already consumed.
			currentByte = skipContent(buffer, currentByte);
			if (currentByte == CR) {
				context.evaluateHeaderValue(from, buffer.position() - 1);
			    context.addHeaderLine();
			} else {
				setCarry(context, from);
			}
			
		} // No carry as it is only whitespace
	}

	private void onRequestLine(HttpProcessingContext context) {
		ByteBuffer buffer = context.getBuffer();
		int from = buffer.position();
		byte currentByte = skipToken(buffer, (byte) 0);
		if (currentByte == SPACE) {
			context.evaluateFirstToken(from, buffer.position() - 1);
			context.setStatus(HttpStatus.REQUEST_LINE_SECOND_TOKEN);
			onRequestSecondToken(context, currentByte);
		} else if (currentByte == CR) {
			throw new HttpException("The request line ended incorrectly");
		} else { // No more bytes in buffer
			setCarry(context, from);
		}
	}

	private void onRequestSecondToken(HttpProcessingContext context, byte currentByte) {
		ByteBuffer buffer = context.getBuffer();
		currentByte = skipWhitespace(buffer, currentByte);
		if (currentByte == CR) {
			throw new HttpException("The request line ended incorrectly after first token");
		} else if (currentByte != SPACE) {
			int from = buffer.position() - 1; // The first byte has been already consumed.
			currentByte = skipToken(buffer, currentByte);
			if (currentByte == SPACE) {
				context.evaluateSecondToken(from, buffer.position() - 1);
				context.setStatus(HttpStatus.REQUEST_LINE_FINAL_CONTENT);
				onRequestFinalContent(context, currentByte);
			} else if (currentByte == CR) {
				throw new HttpException("The request line ended incorrectly at the second token");
			} else {
				setCarry(context, from);
			}
			
		} // No carry as it is only whitespace
	}

	private void onRequestFinalContent(HttpProcessingContext context, byte currentByte) {
		ByteBuffer buffer = context.getBuffer();
		currentByte = skipWhitespace(buffer, currentByte);
		if (currentByte == CR) {
			throw new HttpException("The request line ended incorrectly after first token");
		} else if (currentByte != SPACE) {
			int from = buffer.position() - 1; // The first byte has been already consumed.
			currentByte = skipContent(buffer, currentByte);
			if (currentByte == CR) {
				context.evaluateFinalContent(from, buffer.position() - 1);
				endRequestLine(context);
			} else {
				setCarry(context, from);
			}
		}
	}

	private byte skipWhitespace(ByteBuffer buffer, byte currentByte) {
		while (buffer.hasRemaining() && currentByte == SPACE) {
		    currentByte = buffer.get();
		}
		return currentByte;
	}

	private byte skipToken(ByteBuffer buffer, byte currentByte) {
		while (buffer.hasRemaining() && currentByte != CR && currentByte != SPACE) {
		    currentByte = buffer.get();
		}
		return currentByte;
	}

	private byte skipContent(ByteBuffer buffer, byte currentByte) {
		while (buffer.hasRemaining() && currentByte != CR) {
		    currentByte = buffer.get();
		}
		return currentByte;
	}

	private void setCarry(HttpProcessingContext context, int from) {
		ByteBuffer buffer = context.getBuffer();
		int arrayOffset = buffer.arrayOffset();
		context.setCarry(Arrays.copyOfRange(buffer.array(), arrayOffset + from, arrayOffset + buffer.limit()));
	}

	private void endRequestLine(HttpProcessingContext context) {
		context.evaluateRequestLine();
	}
}
