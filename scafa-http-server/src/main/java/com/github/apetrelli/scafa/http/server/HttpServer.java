package com.github.apetrelli.scafa.http.server;

import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Path;

import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.proto.aio.BufferContextReader;

public interface HttpServer {

	void response(AsynchronousSocketChannel channel, HttpResponse response, CompletionHandler<Void, Void> completionHandler);

	void response(AsynchronousSocketChannel channel, HttpResponse response, BufferContextReader payloadReader, CompletionHandler<Void, Void> completionHandler);

	void response(AsynchronousSocketChannel channel, HttpResponse response, BufferContextReader payloadReader, long size, CompletionHandler<Void, Void> completionHandler);

	void response(AsynchronousSocketChannel channel, HttpResponse response, Path payload, CompletionHandler<Void, Void> completionHandler);
}
