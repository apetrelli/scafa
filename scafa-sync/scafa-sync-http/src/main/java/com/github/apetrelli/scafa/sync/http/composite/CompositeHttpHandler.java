package com.github.apetrelli.scafa.sync.http.composite;

import java.util.regex.Pattern;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.proto.io.FlowBuffer;
import com.github.apetrelli.scafa.sync.http.HttpHandler;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CompositeHttpHandler implements HttpHandler {

	public static class PatternHandlerPair {
		private Pattern pattern;

		private HttpHandler handler;

		public PatternHandlerPair(String pattern, HttpHandler handler) {
			this.pattern = Pattern.compile(pattern);
			this.handler = handler;
		}
	}

	private final HttpHandler defaultHandler;

	private final PatternHandlerPair[] pairs;

	private HttpHandler currentHandler;

	@Override
	public void onConnect() {
		// Does nothing
	}

	@Override
	public void onDisconnect() {
		// Does nothing
	}

	@Override
	public void onStart() {
		currentHandler = defaultHandler;
	}
	
	@Override
	public void onResponseHeader(HttpResponse response) {
        throw new UnsupportedOperationException("This is for requests only");
	}
	
	@Override
	public void onRequestHeader(HttpRequest request) {
		HttpHandler foundHandler = defaultHandler;
		boolean found = false;
		for (int i = 0; i < pairs.length && !found; i++) {
			if (pairs[i].pattern.matcher(request.getParsedResource().getResource()).matches()) {
				foundHandler = pairs[i].handler;
				found = true;
			}
		}
		currentHandler = foundHandler;
		currentHandler.onRequestHeader(request);
	}

	@Override
	public void onBody(FlowBuffer buffer, long offset, long length) {
		currentHandler.onBody(buffer, offset, length);
	}
	
	@Override
	public void onChunkStart(long totalOffset, long chunkLength) {
		currentHandler.onChunkStart(totalOffset, chunkLength);
	}

	@Override
	public void onChunk(FlowBuffer buffer, long totalOffset, long chunkOffset, long chunkLength) {
		currentHandler.onChunk(buffer, totalOffset, chunkOffset, chunkLength);
	}

	@Override
	public void onChunkEnd() {
		currentHandler.onChunkEnd();
	}

	@Override
	public void onChunkedTransferEnd() {
		currentHandler.onChunkedTransferEnd();
	}

	@Override
	public void onDataToPassAlong(FlowBuffer buffer) {
		currentHandler.onDataToPassAlong(buffer);
	}

	@Override
	public void onEnd() {
		currentHandler.onEnd();
		currentHandler = defaultHandler;
	}

	@Override
	public void onError(Throwable exc) {
		if (currentHandler != null) {
			currentHandler.onError(exc);
		}
	}
}
