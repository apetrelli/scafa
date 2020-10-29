package com.github.apetrelli.scafa.http.sync;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.http.HttpException;
import com.github.apetrelli.scafa.http.HttpProcessingContext;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.HttpSink;

public class SyncHttpSink implements HttpSink<HttpHandler, Void> {
	
	private static final Logger LOG = Logger.getLogger(SyncHttpSink.class.getName());
	
	@Override
	public void onStart(HttpHandler handler) {
		handler.onStart();
	}

	public Void endHeader(HttpProcessingContext context, HttpHandler handler) {
		HttpRequest request = context.getRequest();
		HttpResponse response = context.getResponse();
		if (request != null) {
			if ("CONNECT".equalsIgnoreCase(request.getMethod())) {
				context.setHttpConnected(true);
			}
			handler.onRequestHeader(new HttpRequest(request));
		} else if (response != null) {
			handler.onResponseHeader(new HttpResponse(response));
		}
		return null;
	}

	public Void endHeaderAndRequest(HttpProcessingContext context, HttpHandler handler) {
		endHeader(context, handler);
		handler.onEnd();
		return null;
	}

	public Void data(HttpProcessingContext context, HttpHandler handler) {
		ByteBuffer buffer = context.getBuffer();
		int oldLimit = buffer.limit();
		int size = oldLimit - buffer.position();
		int sizeToSend = (int) Math.min(size, context.getCountdown());
		buffer.limit(buffer.position() + sizeToSend);
		handler.onBody(buffer, context.getBodyOffset(), context.getBodySize());
		context.reduceBody(sizeToSend);
		buffer.limit(oldLimit);
		if (context.getCountdown() <= 0L) {
			handler.onEnd();
		}
		return null;
	}

	public Void chunkData(HttpProcessingContext context, HttpHandler handler) {
		ByteBuffer buffer = context.getBuffer();
		int oldLimit = buffer.limit();
		int size = oldLimit - buffer.position();
		int sizeToSend = (int) Math.min(size, context.getCountdown());
		buffer.limit(buffer.position() + sizeToSend);
		handler.onChunk(buffer, context.getTotalChunkedTransferLength() - context.getChunkLength(),
				context.getChunkOffset(), context.getChunkLength());
		context.reduceChunk(sizeToSend);
		buffer.limit(oldLimit);
		if (LOG.isLoggable(Level.FINEST)) {
			LOG.log(Level.FINEST, "Handling chunk from {0} to {1}",
					new Object[] { context.getChunkOffset(), context.getChunkOffset() + sizeToSend });
		}
		return null;
	}

	public Void endChunkCount(HttpProcessingContext context, HttpHandler handler) {
		try {
			context.evaluateChunkLength();
			long chunkCount = context.getChunkLength();
			handler.onChunkStart(context.getTotalChunkedTransferLength(), chunkCount);
			if (chunkCount == 0L) {
				handler.onChunkEnd();
				handler.onChunkedTransferEnd();
				handler.onEnd();
			}
			return null;
		} catch (IOException e) {
			throw new HttpException(e);
		}
	}

	@Override
	public Void onChunkEnd(HttpHandler handler) {
		handler.onChunkEnd();
		return null;
	}
	
	@Override
	public Void onDataToPassAlong(HttpProcessingContext context, HttpHandler handler) {
		handler.onDataToPassAlong(context.getBuffer());
		return null;
	}
	
	@Override
	public Void completed() {
		return null;
	}
}
