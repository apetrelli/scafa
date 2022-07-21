package com.github.apetrelli.scafa.sync.http.impl;

import static com.github.apetrelli.scafa.http.HttpHeaders.CONNECT;

import com.github.apetrelli.scafa.http.HttpProcessingContext;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.HttpSink;
import com.github.apetrelli.scafa.proto.io.FlowBuffer;
import com.github.apetrelli.scafa.sync.http.HttpHandler;

public class SyncHttpSink implements HttpSink<HttpHandler> {
	
	@Override
	public void onStart(HttpHandler handler) {
		handler.onStart();
	}

	public void endHeader(HttpProcessingContext context, HttpHandler handler) {
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
	}

	public void endHeaderAndRequest(HttpProcessingContext context, HttpHandler handler) {
		endHeader(context, handler);
		handler.onEnd();
	}

	public boolean data(HttpProcessingContext context, HttpHandler handler) {
		boolean retValue = false;
		long countdown = context.getCountdown();
		while (countdown > 0) {
			FlowBuffer buffer = context.in().read(countdown);
			handler.onBody(buffer, context.getBodyOffset(), context.getBodySize());
			context.reduceBody(buffer.length());
			countdown = context.getCountdown();
		}
		if (countdown <= 0L) {
			handler.onEnd();
			retValue = true;
		}
		return retValue;
	}

	public void chunkData(HttpProcessingContext context, HttpHandler handler) {
		long countdown = context.getCountdown();
		while (countdown > 0) {
			FlowBuffer buffer = context.in().read(countdown);
			handler.onChunk(buffer, context.getTotalChunkedTransferLength() - context.getChunkLength(),
					context.getChunkOffset(), context.getChunkLength());
			context.reduceChunk(buffer.length());
			countdown = context.getCountdown();
		}
	}

	public boolean endChunkCount(HttpProcessingContext context, HttpHandler handler) {
		boolean retValue = false;
		long chunkCount = context.getChunkLength();
		handler.onChunkStart(context.getTotalChunkedTransferLength(), chunkCount);
		if (chunkCount == 0L) {
			handler.onChunkEnd();
			handler.onChunkedTransferEnd();
			handler.onEnd();
			retValue = true;
		}
		return retValue;
	}

	@Override
	public void onChunkEnd(HttpHandler handler) {
		handler.onChunkEnd();
	}
	
	@Override
	public void onDataToPassAlong(HttpProcessingContext context, HttpHandler handler) {
		handler.onDataToPassAlong(context.in().readBuffer());
	}
}
