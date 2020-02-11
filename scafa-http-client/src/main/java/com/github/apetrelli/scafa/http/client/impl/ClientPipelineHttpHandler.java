package com.github.apetrelli.scafa.http.client.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.http.HttpHandler;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.client.HttpClientConnection;
import com.github.apetrelli.scafa.http.client.HttpClientHandler;
import com.github.apetrelli.scafa.proto.aio.DelegateFailureCompletionHandler;

public class ClientPipelineHttpHandler implements HttpHandler {

    private static final Logger LOG = Logger.getLogger(ClientPipelineHttpHandler.class.getName());

    private static final HttpClientHandler NULL_HANDLER = new NullHttpClientHandler();

    private ConcurrentLinkedQueue<HttpPipelineContext> contexts = new ConcurrentLinkedQueue<>();

    private HttpPipelineContext currentContext = new HttpPipelineContext(null, NULL_HANDLER);

    private HttpClientConnection connection;

    public ClientPipelineHttpHandler(HttpClientConnection connection) {
        this.connection = connection;
    }

    public void add(HttpRequest request, HttpClientHandler handler, HttpClientConnection connection) {
        contexts.offer(new HttpPipelineContext(request, handler));
    }

    @Override
    public void onConnect() throws IOException {
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
    public void onResponseHeader(HttpResponse response, CompletionHandler<Void, Void> handler) {
        currentContext.setResponse(response);
        currentContext.getHandler().onResponseHeader(currentContext.getRequest(), response, handler);
    }

    @Override
    public void onRequestHeader(HttpRequest request, CompletionHandler<Void, Void> handler) {
        handler.failed(new UnsupportedOperationException("This is for responses only"), null);
    }

    @Override
    public void onBody(ByteBuffer buffer, long offset, long length, CompletionHandler<Void, Void> handler) {
        currentContext.getHandler().onBody(currentContext.getRequest(), currentContext.getResponse(), buffer, offset, length, handler);
    }

    @Override
    public void onChunkStart(long totalOffset, long chunkLength, CompletionHandler<Void, Void> handler) {
        handler.completed(null, null); // Go on, nothing to call here.
    }

    @Override
    public void onChunk(ByteBuffer buffer, long totalOffset, long chunkOffset, long chunkLength,
            CompletionHandler<Void, Void> handler) {
        currentContext.getHandler().onBody(currentContext.getRequest(), currentContext.getResponse(), buffer, totalOffset, -1L, handler);
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
    public void onEnd(CompletionHandler<Void, Void> handler) {
        HttpResponse response = currentContext.getResponse();
        currentContext.getHandler().onEnd(currentContext.getRequest(), response, new DelegateFailureCompletionHandler<Void, Void>(handler) {

            @Override
            public void completed(Void result, Void attachment) {
                if (response != null && "close".equals(response.getHeader("Connection"))) {
                    try {
                        connection.close();
                    } catch (IOException e) {
                        LOG.log(Level.SEVERE, "Cannot close connection because the request has an invalid host:port", e);
                    }
                }
                handler.completed(null, null);
            }
        });
    }

}
