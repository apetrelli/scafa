package com.github.apetrelli.scafa.sync.http.impl;

import static com.github.apetrelli.scafa.http.HttpHeaders.CONNECT;

import java.nio.ByteBuffer;
import java.util.logging.Level;

import com.github.apetrelli.scafa.http.HttpProcessingContext;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.HttpSink;
import com.github.apetrelli.scafa.sync.http.HttpHandler;

import lombok.extern.java.Log;

@Log
public class SyncHttpSink implements HttpSink<HttpHandler, Void> {
	
	@Override
	public void onStart(HttpHandler handler) {
		handler.onStart();
	}

	public Void endHeader(HttpProcessingContext context, HttpHandler handler) {
		HttpRequest request = context.getRequest();
		HttpResponse response = context.getResponse();
		if (request != null) {
			if (CONNECT.equals(request.getMethod())) {
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
		if (log.isLoggable(Level.FINEST)) {
			log.log(Level.FINEST, "Handling chunk from {0} to {1}",
					new Object[] { context.getChunkOffset(), context.getChunkOffset() + sizeToSend });
		}
		return null;
	}

	public Void endChunkCount(HttpProcessingContext context, HttpHandler handler) {
		long chunkCount = context.getChunkLength();
		handler.onChunkStart(context.getTotalChunkedTransferLength(), chunkCount);
		if (chunkCount == 0L) {
			handler.onChunkEnd();
			handler.onChunkedTransferEnd();
			handler.onEnd();
		}
		return null;
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
