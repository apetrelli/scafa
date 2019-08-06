package com.github.apetrelli.scafa.http.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.http.HttpBodyMode;
import com.github.apetrelli.scafa.http.HttpByteSink;
import com.github.apetrelli.scafa.http.HttpHandler;
import com.github.apetrelli.scafa.http.HttpInput;
import com.github.apetrelli.scafa.http.HttpProcessingContext;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.HttpStatus;
import com.github.apetrelli.scafa.proto.aio.DelegateFailureCompletionHandler;
import com.github.apetrelli.scafa.proto.processor.ProtocolStateMachine;

public class HttpStateMachine implements ProtocolStateMachine<HttpInput, HttpByteSink, HttpHandler, HttpStatus, HttpProcessingContext> {

	private static final Logger LOG = Logger.getLogger(HttpStateMachine.class.getName());

    private static final byte CR = 13;

    private static final byte LF = 10;

	@Override
	public HttpStatus next(HttpProcessingContext context) {
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
	}

	@Override
	public void out(HttpProcessingContext context, HttpHandler handler, CompletionHandler<Void, Void> completionHandler) {
		HttpInput input = context.getInput();
		switch (context.getStatus()) {
		case IDLE:
	        context.reset();
	        context.appendToLine(input.getBuffer().get());
            completionHandler.completed(null, null);
			break;
		case REQUEST_LINE:
            onRequestLine(handler, completionHandler, context);
			break;
		case REQUEST_LINE_CR:
			endRequestLine(completionHandler, context);
			break;
		case REQUEST_LINE_LF:
            input.getBuffer().get(); // discard LF.
            completionHandler.completed(null, null);
			break;
		case POSSIBLE_HEADER_CR:
            input.getBuffer().get(); // discard CR.
            context.evaluateBodyMode();
            completionHandler.completed(null, null);
			break;
		case SEND_HEADER_AND_END:
            input.getBuffer().get(); // discard LF.
            endHeaderAndRequest(context, handler, completionHandler);
			break;
		case POSSIBLE_HEADER_LF:
            input.getBuffer().get(); // discard LF.
            endHeader(context, handler, completionHandler);
			break;
		case HEADER:
            onHeader(context, completionHandler);
			break;
		case HEADER_CR:
            input.getBuffer().get(); // discard CR
            context.addHeaderLine();
            completionHandler.completed(null, null);
			break;
		case HEADER_LF:
            input.getBuffer().get(); // discard LF
            completionHandler.completed(null, null);
			break;
		case BODY:
            data(context, handler, completionHandler);
			break;
		case PENULTIMATE_BYTE:
			input.getBuffer().get(); // discard CR
            completionHandler.completed(null, null);
			break;
		case LAST_BYTE:
			input.getBuffer().get(); // discard LF
            completionHandler.completed(null, null);
			break;
		case CHUNK_COUNT:
            onChunkCount(context, handler, completionHandler);
			break;
		case CHUNK_COUNT_CR:
            input.getBuffer().get(); // discard CR.
            completionHandler.completed(null, null);
			break;
		case CHUNK_COUNT_LF:
            input.getBuffer().get(); // discard LF.
            endChunkCount(context, handler, completionHandler);
			break;
		case CHUNK:
            chunkData(context, handler, completionHandler);
			break;
		case CHUNK_CR:
			input.getBuffer().get(); // discard CR
			handler.onChunkEnd(completionHandler);
			break;
		case CHUNK_LF:
            input.getBuffer().get(); // discard LF.
            completionHandler.completed(null, null);
			break;
		case CONNECT:
			handler.onDataToPassAlong(input.getBuffer(), completionHandler);
		}
	}

	private void onChunkCount(HttpProcessingContext context, HttpHandler handler, CompletionHandler<Void, Void> completionHandler) {
		ByteBuffer buffer = context.getInput().getBuffer();
		byte currentByte;
		currentByte = buffer.get();
		while (buffer.hasRemaining() && currentByte != CR) {
			context.appendToLine(currentByte);
		    currentByte = buffer.get();
		}
		// Evaluating of CR is not necessary since chunk count is treated when LF arrives, at the next state.
		completionHandler.completed(null, null);
	}

	private void onHeader(HttpProcessingContext context, CompletionHandler<Void, Void> completionHandler) {
		ByteBuffer buffer = context.getInput().getBuffer();
		byte currentByte;
		currentByte = buffer.get();
		while (buffer.hasRemaining() && currentByte != CR) {
			context.appendToLine(currentByte);
		    currentByte = buffer.get();
		}
		if (currentByte == CR) {
		    context.addHeaderLine();
		}
		completionHandler.completed(null, null);
	}

	private void onRequestLine(HttpHandler handler, CompletionHandler<Void, Void> completionHandler, HttpProcessingContext context) {
		ByteBuffer buffer = context.getInput().getBuffer();
		byte currentByte;
		currentByte = buffer.get();
		while (buffer.hasRemaining() && currentByte != CR) {
		    context.appendToLine(currentByte);
		    currentByte = buffer.get();
		}
		if (currentByte == CR) {
			endRequestLine(completionHandler, context);
		} else {
			completionHandler.completed(null, null);
		}
	}

	private void endRequestLine(CompletionHandler<Void, Void> completionHandler, HttpProcessingContext context) {
		try {
			context.evaluateRequestLine();
		    completionHandler.completed(null, null);
		} catch (IOException e) {
			completionHandler.failed(e, null);
		}
	}

    public void endHeader(HttpProcessingContext context, HttpHandler handler, CompletionHandler<Void, Void> completionHandler) {
    	HttpRequest request = context.getRequest();
    	HttpResponse response = context.getResponse();
        if (request != null) {
            if ("CONNECT".equalsIgnoreCase(request.getMethod())) {
                context.getInput().setHttpConnected(true);
            }
            handler.onRequestHeader(new HttpRequest(request), completionHandler);
        } else if (response != null) {
            handler.onResponseHeader(new HttpResponse(response), completionHandler);
        } else {
            completionHandler.completed(null, null);
        }
    }

    private void endHeaderAndRequest(HttpProcessingContext context, HttpHandler handler, CompletionHandler<Void, Void> completionHandler) {
        endHeader(context, handler, new DelegateFailureCompletionHandler<Void, Void>(completionHandler) {

            @Override
            public void completed(Void result, Void attachment) {
                handler.onEnd();
                completionHandler.completed(result, attachment);
            }
        });

    }

    private void data(HttpProcessingContext context, HttpHandler handler, CompletionHandler<Void, Void> completionHandler) {
    	HttpInput input = context.getInput();
        ByteBuffer buffer = input.getBuffer();
        int oldLimit = buffer.limit();
        int size = oldLimit - buffer.position();
        int sizeToSend = (int) Math.min(size, input.getCountdown());
        buffer.limit(buffer.position() + sizeToSend);
        handler.onBody(buffer, input.getBodyOffset(), input.getBodySize(), new DelegateFailureCompletionHandler<Void, Void>(completionHandler) {

            @Override
            public void completed(Void result, Void attachment) {
                input.reduceBody(sizeToSend);
                buffer.limit(oldLimit);
                if (input.getCountdown() <= 0L) {
                    handler.onEnd();
                }
                completionHandler.completed(result, attachment);
            }
        });
    }

    private void chunkData(HttpProcessingContext context, HttpHandler handler, CompletionHandler<Void, Void> completionHandler) {
    	HttpInput input = context.getInput();
        ByteBuffer buffer = input.getBuffer();
        int oldLimit = buffer.limit();
        int size = oldLimit - buffer.position();
        int sizeToSend = (int) Math.min(size, input.getCountdown());
        buffer.limit(buffer.position() + sizeToSend);
        handler.onChunk(buffer,
                input.getTotalChunkedTransferLength() - input.getChunkLength(), input.getChunkOffset(),
                input.getChunkLength(), new DelegateFailureCompletionHandler<Void, Void>(completionHandler) {

                    @Override
                    public void completed(Void result, Void attachment) {
                        input.reduceChunk(sizeToSend);
                        buffer.limit(oldLimit);
                        if (LOG.isLoggable(Level.FINEST)) {
                            LOG.log(Level.FINEST, "Handling chunk from {0} to {1}",
                                    new Object[] { input.getChunkOffset(), input.getChunkOffset() + sizeToSend });
                        }
                        completionHandler.completed(result, attachment);
                    }
                });
    }

    private void endChunkCount(HttpProcessingContext context, HttpHandler handler, CompletionHandler<Void, Void> completionHandler) {
    	try {
			context.evaluateChunkLength();
	    	HttpInput input = context.getInput();
	    	long chunkCount = input.getChunkLength();
	        handler.onChunkStart(input.getTotalChunkedTransferLength(), chunkCount, new DelegateFailureCompletionHandler<Void, Void>(completionHandler) {

	            @Override
	            public void completed(Void result, Void attachment) {
	                if (chunkCount == 0L) {
	                    handler.onChunkEnd(new DelegateFailureCompletionHandler<Void, Void>(completionHandler) {

	                        @Override
	                        public void completed(Void result, Void attachment) {
	                            handler.onChunkedTransferEnd(new DelegateFailureCompletionHandler<Void, Void>(completionHandler) {

	                                @Override
	                                public void completed(Void result, Void attachment) {
	                                    handler.onEnd();
	                                    completionHandler.completed(result, attachment);
	                                }
	                            });
	                        }
	                    });
	                } else {
	                    completionHandler.completed(result, attachment);
	                }
	            }
	        });
		} catch (IOException e) {
			completionHandler.failed(e, null);
		}
    }
}
