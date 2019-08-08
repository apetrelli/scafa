package com.github.apetrelli.scafa.http.client.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.http.HttpConnection;
import com.github.apetrelli.scafa.http.HttpHandler;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.client.HttpClientHandler;

public class ClientPipelineHttpHandler implements HttpHandler {

	private static final Logger LOG = Logger.getLogger(ClientPipelineHttpHandler.class.getName());

	private static final HttpClientHandler NULL_HANDLER = new NullHttpClientHandler();

	private ConcurrentLinkedQueue<HttpPipelineContext> contexts = new ConcurrentLinkedQueue<>();

	private HttpPipelineContext currentContext = new HttpPipelineContext(NULL_HANDLER, null);

	private HttpConnection connection = null;

	public void add(HttpClientHandler handler, HttpRequest request) {
		contexts.offer(new HttpPipelineContext(handler, request));
	}

	public void setConnection(HttpConnection connection) {
		this.connection = connection;
	}

	@Override
	public void onConnect() throws IOException {
	}

	@Override
	public void onDisconnect() throws IOException {
		contexts.clear();
		currentContext = new HttpPipelineContext(NULL_HANDLER, null);
	}

	@Override
	public void onStart() {
		currentContext = contexts.poll();
		if (currentContext == null) {
			currentContext = new HttpPipelineContext(NULL_HANDLER, null);
		}
		currentContext.setConnection(connection);
		connection = null;
		currentContext.getHandler().onStart();
	}

	@Override
	public void onResponseHeader(HttpResponse response, CompletionHandler<Void, Void> handler) {
		currentContext.setResponse(response);
		currentContext.getHandler().onResponseHeader(response, handler);
	}

	@Override
	public void onRequestHeader(HttpRequest request, CompletionHandler<Void, Void> handler) {
		handler.failed(new UnsupportedOperationException("This is for responses only"), null);
	}

	@Override
	public void onBody(ByteBuffer buffer, long offset, long length, CompletionHandler<Void, Void> handler) {
		currentContext.getHandler().onBody(buffer, offset, length, handler);
	}

	@Override
	public void onChunkStart(long totalOffset, long chunkLength, CompletionHandler<Void, Void> handler) {
		handler.completed(null, null); // Go on, nothing to call here.
	}

	@Override
	public void onChunk(ByteBuffer buffer, long totalOffset, long chunkOffset, long chunkLength,
			CompletionHandler<Void, Void> handler) {
		currentContext.getHandler().onBody(buffer, totalOffset, -1L, handler);
	}

	@Override
	public void onChunkEnd(CompletionHandler<Void, Void> handler) {
		handler.completed(null, null); // Go on, nothing to call here.
	}

	@Override
	public void onChunkedTransferEnd(CompletionHandler<Void, Void> handler) {
		handler.completed(null, null); // Go on, nothing to call here.
	}

	@Override
	public void onDataToPassAlong(ByteBuffer buffer, CompletionHandler<Void, Void> handler) {
		handler.failed(new UnsupportedOperationException("CONNECT method not supported"), null);
	}

	@Override
	public void onEnd() {
		currentContext.getHandler().onEnd();
		HttpResponse response = currentContext.getResponse();
		HttpConnection connection = currentContext.getConnection();
		if (response != null && "close".equals(response.getHeader("Connection")) && connection != null) {
			try {
				connection.close();
			} catch (IOException e) {
				LOG.log(Level.SEVERE, "Cannot close connection because the request has an invalid host:port", e);
			}
		}
	}

}
