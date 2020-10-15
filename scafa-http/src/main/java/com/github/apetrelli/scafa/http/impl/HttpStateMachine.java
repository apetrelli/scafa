package com.github.apetrelli.scafa.http.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.http.HttpBodyMode;
import com.github.apetrelli.scafa.http.HttpHandler;
import com.github.apetrelli.scafa.http.HttpProcessingContext;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.HttpStatus;
import com.github.apetrelli.scafa.proto.aio.CompletionHandlerFuture;
import com.github.apetrelli.scafa.proto.processor.ProtocolStateMachine;

public class HttpStateMachine implements ProtocolStateMachine<HttpHandler, HttpStatus, HttpProcessingContext> {

	private static final Logger LOG = Logger.getLogger(HttpStateMachine.class.getName());

    private static final byte CR = 13;

    private static final byte LF = 10;

	@Override
	public HttpStatus next(HttpProcessingContext context) {
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
	
	@Override
	public CompletableFuture<Void> out(HttpProcessingContext context,
			HttpHandler handler) {
		CompletableFuture<Void> retValue = null;
		switch (context.getStatus()) {
		case IDLE:
			retValue = CompletionHandlerFuture.completeEmpty();
			break;
		case START_REQUEST_LINE:
	        context.reset();
	        context.appendToLine(context.getBuffer().get());
			handler.onStart();
			retValue = CompletionHandlerFuture.completeEmpty();
			break;
		case REQUEST_LINE:
            retValue = onRequestLine(context);
			break;
		case REQUEST_LINE_CR:
			retValue = endRequestLine(context);
			break;
		case REQUEST_LINE_LF:
		case HEADER_LF:
		case PENULTIMATE_BYTE:
		case LAST_BYTE:
		case CHUNK_COUNT_CR:
		case CHUNK_LF:
            context.getBuffer().get(); // discard LF.
			retValue = CompletionHandlerFuture.completeEmpty();
			break;
		case POSSIBLE_HEADER_CR:
            context.getBuffer().get(); // discard CR.
            context.evaluateBodyMode();
			retValue = CompletionHandlerFuture.completeEmpty();
			break;
		case SEND_HEADER_AND_END:
            context.getBuffer().get(); // discard LF.
            retValue = endHeaderAndRequest(context, handler);
			break;
		case POSSIBLE_HEADER_LF:
            context.getBuffer().get(); // discard LF.
            retValue = endHeader(context, handler);
			break;
		case HEADER:
			retValue = onHeader(context);
			break;
		case HEADER_CR:
            context.getBuffer().get(); // discard CR
            context.addHeaderLine();
			retValue = CompletionHandlerFuture.completeEmpty();
			break;
		case BODY:
            retValue = data(context, handler);
			break;
		case CHUNK_COUNT:
            retValue = onChunkCount(context);
			break;
		case CHUNK_COUNT_LF:
            context.getBuffer().get(); // discard LF.
            retValue = endChunkCount(context, handler);
			break;
		case CHUNK:
            retValue = chunkData(context, handler);
			break;
		case CHUNK_CR:
			context.getBuffer().get(); // discard CR
			retValue = handler.onChunkEnd();
			break;
		case CONNECT:
			retValue = handler.onDataToPassAlong(context.getBuffer());
		}
		return retValue;
	}

	private CompletableFuture<Void> onChunkCount(HttpProcessingContext context) {
		ByteBuffer buffer = context.getBuffer();
		byte currentByte;
		currentByte = buffer.get();
		while (buffer.hasRemaining() && currentByte != CR) {
			context.appendToLine(currentByte);
		    currentByte = buffer.get();
		}
		// Evaluating of CR is not necessary since chunk count is treated when LF arrives, at the next state.
		return CompletionHandlerFuture.completeEmpty();
	}

	private CompletableFuture<Void> onHeader(HttpProcessingContext context) {
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
		return CompletionHandlerFuture.completeEmpty();
	}

	private CompletableFuture<Void> onRequestLine(HttpProcessingContext context) {
		ByteBuffer buffer = context.getBuffer();
		byte currentByte;
		currentByte = buffer.get();
		while (buffer.hasRemaining() && currentByte != CR) {
		    context.appendToLine(currentByte);
		    currentByte = buffer.get();
		}
		if (currentByte == CR) {
			return endRequestLine(context);
		} else {
			return CompletionHandlerFuture.completeEmpty();
		}
	}

	private CompletableFuture<Void> endRequestLine(HttpProcessingContext context) {
		try {
			context.evaluateRequestLine();
			return CompletionHandlerFuture.completeEmpty();
		} catch (IOException e) {
			return CompletableFuture.failedFuture(e);
		}
	}

    private CompletableFuture<Void> endHeader(HttpProcessingContext context, HttpHandler handler) {
    	HttpRequest request = context.getRequest();
    	HttpResponse response = context.getResponse();
        if (request != null) {
            if ("CONNECT".equalsIgnoreCase(request.getMethod())) {
                context.setHttpConnected(true);
            }
            return handler.onRequestHeader(new HttpRequest(request));
        } else if (response != null) {
            return handler.onResponseHeader(new HttpResponse(response));
        } else {
            return CompletionHandlerFuture.completeEmpty();
        }
    }

    private CompletableFuture<Void> endHeaderAndRequest(HttpProcessingContext context, HttpHandler handler) {
    	return endHeader(context, handler).thenCompose(x -> handler.onEnd());
    }

    private CompletableFuture<Void> data(HttpProcessingContext context, HttpHandler handler) {
    	ByteBuffer buffer = context.getBuffer();
        int oldLimit = buffer.limit();
        int size = oldLimit - buffer.position();
        int sizeToSend = (int) Math.min(size, context.getCountdown());
        buffer.limit(buffer.position() + sizeToSend);
        return handler.onBody(buffer, context.getBodyOffset(), context.getBodySize()).thenCompose(x -> {
            context.reduceBody(sizeToSend);
            buffer.limit(oldLimit);
            if (context.getCountdown() <= 0L) {
                return handler.onEnd();
            } else {
            	return CompletionHandlerFuture.completeEmpty();
            }
        });
    }

    private CompletableFuture<Void> chunkData(HttpProcessingContext context, HttpHandler handler) {
		ByteBuffer buffer = context.getBuffer();
		int oldLimit = buffer.limit();
		int size = oldLimit - buffer.position();
		int sizeToSend = (int) Math.min(size, context.getCountdown());
		buffer.limit(buffer.position() + sizeToSend);
		return handler.onChunk(buffer, context.getTotalChunkedTransferLength() - context.getChunkLength(),
				context.getChunkOffset(), context.getChunkLength()).thenCompose(x -> {
					context.reduceChunk(sizeToSend);
					buffer.limit(oldLimit);
					if (LOG.isLoggable(Level.FINEST)) {
						LOG.log(Level.FINEST, "Handling chunk from {0} to {1}",
								new Object[] { context.getChunkOffset(), context.getChunkOffset() + sizeToSend });
					}
					return CompletionHandlerFuture.completeEmpty();
				});
    }

    private CompletableFuture<Void> endChunkCount(HttpProcessingContext context, HttpHandler handler) {
    	try {
			context.evaluateChunkLength();
			long chunkCount = context.getChunkLength();
			return handler.onChunkStart(context.getTotalChunkedTransferLength(), chunkCount).thenCompose(x -> {
                if (chunkCount == 0L) {
                	return handler.onChunkEnd().thenCompose(y -> handler.onChunkedTransferEnd()).thenCompose(z -> handler.onEnd());
                } else {
                	return CompletionHandlerFuture.completeEmpty();
                }
				
			});
		} catch (IOException e) {
			return CompletableFuture.failedFuture(e);
		}
    }
}
