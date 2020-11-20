package com.github.apetrelli.scafa.http.server.sync;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Path;

import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.sync.HttpSyncSocket;

public interface HttpServer {

	void response(HttpSyncSocket<HttpResponse> channel, HttpResponse response);

	void response(HttpSyncSocket<HttpResponse> channel, HttpResponse response, InputStream payload, ByteBuffer writeBuffer);

	void response(HttpSyncSocket<HttpResponse> channel, HttpResponse response, InputStream payload, long size, ByteBuffer writeBuffer);

	void response(HttpSyncSocket<HttpResponse> channel, HttpResponse response, Path payload, ByteBuffer writeBuffer);
}
