package com.github.apetrelli.scafa.http.server.sync;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.sync.HttpSyncSocket;
import com.github.apetrelli.scafa.proto.aio.BufferContextReader;

public interface HttpServer {

	void response(HttpSyncSocket<HttpResponse> channel, HttpResponse response);

	void response(HttpSyncSocket<HttpResponse> channel, HttpResponse response, BufferContextReader payloadReader, ByteBuffer writeBuffer);

	CompletableFuture<Void> response(HttpAsyncSocket<HttpResponse> channel, HttpResponse response, BufferContextReader payloadReader, long size, ByteBuffer writeBuffer);

	CompletableFuture<Void> response(HttpAsyncSocket<HttpResponse> channel, HttpResponse response, Path payload, ByteBuffer writeBuffer);
}
