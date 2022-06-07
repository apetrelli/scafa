package com.github.apetrelli.scafa.http.client.impl;

import static com.github.apetrelli.scafa.http.HttpHeaders.CLOSE_CONNECTION;
import static com.github.apetrelli.scafa.http.HttpHeaders.CONNECTION;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.github.apetrelli.scafa.async.proto.util.CompletionHandlerFuture;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.async.http.HttpHandler;
import com.github.apetrelli.scafa.async.http.impl.HttpHandlerSupport;
import com.github.apetrelli.scafa.http.client.HttpClientConnection;
import com.github.apetrelli.scafa.http.client.HttpClientHandler;

public class ClientPipelineHttpHandler extends HttpHandlerSupport implements HttpHandler {

	private static final HttpClientHandler NULL_HANDLER = new NullHttpClientHandler();

    private ConcurrentLinkedQueue<HttpPipelineContext> contexts = new ConcurrentLinkedQueue<>();

    private HttpPipelineContext currentContext = new HttpPipelineContext(null, NULL_HANDLER);

    private HttpClientConnection connection;

    public ClientPipelineHttpHandler(HttpClientConnection connection) {
        this.connection = connection;
    }

    public void add(HttpRequest request, HttpClientHandler handler) {
        contexts.offer(new HttpPipelineContext(request, handler));
    }

    @Override
    public void onDisconnect() {
        contexts.clear();
        currentContext = new HttpPipelineContext(null, NULL_HANDLER);
    }

    @Override
    public void onStart() {
        currentContext = contexts.poll();
        if (currentContext == null) {
            currentContext = new HttpPipelineContext(null, NULL_HANDLER);
        }
        currentContext.getHandler().onStart();
    }
    
    @Override
    public CompletableFuture<Void> onResponseHeader(HttpResponse response) {
        currentContext.setResponse(response);
        return currentContext.getHandler().onResponseHeader(currentContext.getRequest(), response);
    }
    
    @Override
    public CompletableFuture<Void> onRequestHeader(HttpRequest request) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("This is for responses only"));
    }

    @Override
    public CompletableFuture<Void> onBody(ByteBuffer buffer, long offset, long length) {
        return currentContext.getHandler().onBody(currentContext.getRequest(), currentContext.getResponse(), buffer, offset, length);
    }

    @Override
    public CompletableFuture<Void> onChunk(ByteBuffer buffer, long totalOffset, long chunkOffset, long chunkLength) {
        return currentContext.getHandler().onBody(currentContext.getRequest(), currentContext.getResponse(), buffer, totalOffset, -1L);
    }
    
    @Override
    public CompletableFuture<Void> onDataToPassAlong(ByteBuffer buffer) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("CONNECT method not supported"));
    }
    
    @Override
    public CompletableFuture<Void> onEnd() {
        HttpResponse response = currentContext.getResponse();
        return currentContext.getHandler().onEnd(currentContext.getRequest(), response)
        		.thenCompose(x -> {
                    if (response != null && CLOSE_CONNECTION.equals(response.getHeader(CONNECTION))) {
                        return connection.disconnect();
                    } else {
                    	return CompletionHandlerFuture.completeEmpty();
                    }
        		});
    }

}
