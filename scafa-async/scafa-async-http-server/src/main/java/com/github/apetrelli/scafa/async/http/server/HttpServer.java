package com.github.apetrelli.scafa.async.http.server;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.async.file.BufferContextReader;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.async.http.HttpAsyncSocket;

public interface HttpServer {

	CompletableFuture<Void> response(HttpAsyncSocket<HttpResponse> channel, HttpResponse response, ByteBuffer writeBuffer);

	CompletableFuture<Void> response(HttpAsyncSocket<HttpResponse> channel, HttpResponse response, BufferContextReader payloadReader, ByteBuffer writeBuffer);

	CompletableFuture<Void> response(HttpAsyncSocket<HttpResponse> channel, HttpResponse response, BufferContextReader payloadReader, long size, ByteBuffer writeBuffer);

	CompletableFuture<Void> response(HttpAsyncSocket<HttpResponse> channel, HttpResponse response, Path payload, ByteBuffer writeBuffer);
}
