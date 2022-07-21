package com.github.apetrelli.scafa.http.impl;

import com.github.apetrelli.scafa.http.HttpBodyMode;
import com.github.apetrelli.scafa.http.HttpException;
import com.github.apetrelli.scafa.http.HttpProcessingContext;
import com.github.apetrelli.scafa.http.HttpSink;
import com.github.apetrelli.scafa.http.HttpStatus;
import com.github.apetrelli.scafa.proto.processor.ProtocolStateMachine;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HttpStateMachine<H> implements ProtocolStateMachine<H, HttpProcessingContext> {

    private static final byte CR = 13;

    private static final byte LF = 10;
    
    private static final byte SPACE = 32;
    
    private static final byte COLON = 58;
    
    private final HttpSink<H> sink;

	@Override
	public void out(HttpProcessingContext context, H handler) {
		boolean finished = false;
		while (!finished) {
			Byte currentByte = next(context);
			switch (context.getStatus()) {
			case IDLE:
				break;
			case START_REQUEST_LINE:
		        context.reset();
				sink.onStart(handler);
				break;
			case REQUEST_LINE_FIRST_TOKEN:
	            onRequestLine(context, currentByte);
				break;
			case REQUEST_LINE_SECOND_TOKEN:
				onRequestSecondToken(context, currentByte);
				break;
			case REQUEST_LINE_FINAL_CONTENT:
				onRequestFinalContent(context, currentByte);
				break;
			case REQUEST_LINE_CR:
	            context.getAndTransferToHeader(currentByte); // discard CR.
				context.evaluateFinalContent(0);
				endRequestLine(context);
				break;
			case REQUEST_LINE_LF:
			case HEADER_LF:
			case PENULTIMATE_BYTE:
			case LAST_BYTE:
			case CHUNK_LF:
	            context.currentOrNextByte(currentByte); // discard LF.
				break;
			case POSSIBLE_HEADER_CR:
	            context.getAndTransferToHeader(currentByte); // discard CR.
	            context.evaluateBodyMode();
				break;
			case SEND_HEADER_AND_END:
	            context.getAndTransferToHeader(currentByte); // discard LF.
				sink.endHeaderAndRequest(context, handler);
				finished = true;
				break;
			case POSSIBLE_HEADER_LF:
	            context.getAndTransferToHeader(currentByte); // discard LF.
	            sink.endHeader(context, handler);
				break;
			case HEADER_NAME:
				onHeader(context, currentByte);
				break;
			case HEADER_VALUE:
				onHeaderValue(context, currentByte);
				break;
			case HEADER_CR:
	            context.getAndTransferToHeader(currentByte); // discard CR
				context.evaluateHeaderValue(0);
	            context.addHeaderLine();
				break;
			case BODY:
				finished = sink.data(context, handler);
				break;
			case CHUNK_COUNT:
				evaluateChunkLength(context, currentByte);
				break;
			case CHUNK_COUNT_CR:
				context.currentOrNextByte(currentByte); // discard CR
				context.evaluateChunkLength();
				break;
			case CHUNK_COUNT_LF:
				context.currentOrNextByte(currentByte); // discard LF.
	            finished = sink.endChunkCount(context, handler);
				break;
			case CHUNK:
				sink.chunkData(context, handler);
				break;
			case CHUNK_CR:
				context.currentOrNextByte(currentByte); // discard CR
				sink.onChunkEnd(handler);
				break;
			case CONNECT:
				sink.onDataToPassAlong(context, handler);
				finished = true;
			}
		}
	}
	
	private Byte next(HttpProcessingContext context) {
		Byte currentByte = null;
		HttpStatus newStatus = null;
		switch (context.getStatus()) {
		case IDLE:
			newStatus = HttpStatus.START_REQUEST_LINE;
			break;
		case START_REQUEST_LINE:
			newStatus = HttpStatus.REQUEST_LINE_FIRST_TOKEN;
			break;
		case REQUEST_LINE_FIRST_TOKEN:
			currentByte = context.in().read();
            switch (currentByte) {
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
			currentByte = context.in().read();
            switch (currentByte) {
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
			currentByte = context.in().read();
            switch (currentByte) {
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
			currentByte = context.in().read();
			newStatus = currentByte == CR ? HttpStatus.POSSIBLE_HEADER_CR : HttpStatus.HEADER_NAME;
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
			currentByte = context.in().read();
            switch (currentByte) {
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
			currentByte = context.in().read();
            switch (currentByte) {
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
			currentByte = context.in().read();
			newStatus = currentByte == CR ? HttpStatus.POSSIBLE_HEADER_CR : HttpStatus.HEADER_NAME;
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
			currentByte = context.in().read();
            switch (currentByte) {
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
		return currentByte;
	}

	private void onHeader(HttpProcessingContext context, Byte currentByte) {
		context.markStart(0);
		context.transferToHeader(currentByte);
		currentByte = context.transferToHeaderBuffer(currentByte != null ? currentByte : 0, x -> x != COLON && x != CR && x != SPACE);
		if (currentByte == COLON) {
			context.evaluateHeaderName(-1);
			context.setStatus(HttpStatus.HEADER_VALUE);
			currentByte = context.getAndTransferToHeader(null); // Go to next whitespace, or real header value.
			onHeaderValue(context, currentByte);
		} else if (currentByte == CR || currentByte == SPACE) {
			throw new HttpException("The header line is invalid");
		}
	}

	private void onHeaderValue(HttpProcessingContext context, Byte currentByte) {
		currentByte = skipWhitespace(context, currentByte);
		if (currentByte == CR) {
			throw new HttpException("The header line ended incorrectly after first token");
		} else if (currentByte != SPACE) {
			context.markStart(-1); // The first byte has been already consumed.
			currentByte = skipContent(context, currentByte);
			if (currentByte == CR) {
				context.evaluateHeaderValue(-1);
			    context.addHeaderLine();
			}
			
		}
	}

	private void onRequestLine(HttpProcessingContext context, Byte inputByte) {
		context.markStart(0);
		byte currentByte = skipToken(context, inputByte);
		if (currentByte == SPACE) {
			context.evaluateFirstToken(-1);
			context.setStatus(HttpStatus.REQUEST_LINE_SECOND_TOKEN);
			onRequestSecondToken(context, currentByte);
		} else if (currentByte == CR) {
			throw new HttpException("The request line ended incorrectly");
		}
	}

	private void onRequestSecondToken(HttpProcessingContext context, Byte currentByte) {
		currentByte = skipWhitespace(context, currentByte);
		if (currentByte == CR) {
			throw new HttpException("The request line ended incorrectly after first token");
		} else if (currentByte != SPACE) {
			context.markStart(-1); // The first byte has been already consumed.
			currentByte = skipToken(context, currentByte);
			if (currentByte == SPACE) {
				context.evaluateSecondToken(-1);
				context.setStatus(HttpStatus.REQUEST_LINE_FINAL_CONTENT);
				onRequestFinalContent(context, currentByte);
			} else if (currentByte == CR) {
				throw new HttpException("The request line ended incorrectly at the second token");
			}
			
		} // No carry as it is only whitespace
	}

	private void onRequestFinalContent(HttpProcessingContext context, Byte currentByte) {
		currentByte = skipWhitespace(context, currentByte);
		if (currentByte == CR) {
			throw new HttpException("The request line ended incorrectly after first token");
		} else if (currentByte != SPACE) {
			context.markStart(-1); // The first byte has been already consumed.
			currentByte = skipContent(context, currentByte);
			if (currentByte == CR) {
				context.evaluateFinalContent(-1);
				endRequestLine(context);
			}
		}
	}

	private byte skipWhitespace(HttpProcessingContext context, Byte currentByte) {
		return context.transferToHeaderBuffer(currentByte != null ? currentByte : SPACE, x -> x == SPACE);
	}

	private byte skipToken(HttpProcessingContext context, Byte currentByte) {
		return context.transferToHeaderBuffer(currentByte != null ? currentByte.byteValue() : 0,
				x -> x != CR && x != SPACE);
	}

	private void evaluateChunkLength(HttpProcessingContext context, Byte inputByte) {
		byte currentByte = inputByte != null ? inputByte : context.in().read();
		while (currentByte != CR) {
			context.addToChunkLength(currentByte);
			currentByte = context.in().read();
		}
		if (currentByte == CR) {
			context.setStatus(HttpStatus.CHUNK_COUNT_CR);
			context.evaluateChunkLength();
		}
	}

	private byte skipContent(HttpProcessingContext context, byte currentByte) {
		return context.transferToHeaderBuffer(currentByte, x -> x != CR);
	}

	private void endRequestLine(HttpProcessingContext context) {
		context.evaluateRequestLine();
	}
}
