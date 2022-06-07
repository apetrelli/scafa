package com.github.apetrelli.scafa.async.http.impl;

import static com.github.apetrelli.scafa.http.HttpHeaders.CONNECT;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.async.http.HttpHandler;
import com.github.apetrelli.scafa.async.proto.util.CompletionHandlerFuture;
import com.github.apetrelli.scafa.http.HttpProcessingContext;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.HttpSink;

public class AsyncHttpSink implements HttpSink<HttpHandler, CompletableFuture<Void>> {
	
	private static final Logger LOG = Logger.getLogger(AsyncHttpSink.class.getName());
	
	@Override
	public void onStart(HttpHandler handler) {
		handler.onStart();
	}

	public CompletableFuture<Void> endHeader(HttpProcessingContext context, HttpHandler handler) {
		HttpRequest request = context.getRequest();
		HttpResponse response = context.getResponse();
		if (request != null) {
			if (CONNECT.equals(request.getMethod())) {
				context.setHttpConnected(true);
			}
			return handler.onRequestHeader(new HttpRequest(request));
		} else if (response != null) {
			return handler.onResponseHeader(new HttpResponse(response));
		} else {
			return CompletionHandlerFuture.completeEmpty();
		}
	}

	public CompletableFuture<Void> endHeaderAndRequest(HttpProcessingContext context, HttpHandler handler) {
		return endHeader(context, handler).thenCompose(x -> handler.onEnd());
	}

	public CompletableFuture<Void> data(HttpProcessingContext context, HttpHandler handler) {
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

	public CompletableFuture<Void> chunkData(HttpProcessingContext context, HttpHandler handler) {
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

	public CompletableFuture<Void> endChunkCount(HttpProcessingContext context, HttpHandler handler) {
		long chunkCount = context.getChunkLength();
		return handler.onChunkStart(context.getTotalChunkedTransferLength(), chunkCount).thenCompose(x -> {
			if (chunkCount == 0L) {
				return handler.onChunkEnd().thenCompose(y -> handler.onChunkedTransferEnd())
						.thenCompose(z -> handler.onEnd());
			} else {
				return CompletionHandlerFuture.completeEmpty();
			}

		});
	}

	@Override
	public CompletableFuture<Void> onChunkEnd(HttpHandler handler) {
		return handler.onChunkEnd();
	}
	
	@Override
	public CompletableFuture<Void> onDataToPassAlong(HttpProcessingContext context, HttpHandler handler) {
		return handler.onDataToPassAlong(context.getBuffer());
	}
	
	@Override
	public CompletableFuture<Void> completed() {
		return CompletionHandlerFuture.completeEmpty();
	}
}
