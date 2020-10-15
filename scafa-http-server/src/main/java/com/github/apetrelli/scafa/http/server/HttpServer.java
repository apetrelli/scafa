package com.github.apetrelli.scafa.http.server;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.proto.aio.BufferContextReader;

public interface HttpServer {

	CompletableFuture<Void> response(HttpAsyncSocket<HttpResponse> channel, HttpResponse response);

	CompletableFuture<Void> response(HttpAsyncSocket<HttpResponse> channel, HttpResponse response, BufferContextReader payloadReader);

	CompletableFuture<Void> response(HttpAsyncSocket<HttpResponse> channel, HttpResponse response, BufferContextReader payloadReader, long size);

	CompletableFuture<Void> response(HttpAsyncSocket<HttpResponse> channel, HttpResponse response, Path payload);
}
