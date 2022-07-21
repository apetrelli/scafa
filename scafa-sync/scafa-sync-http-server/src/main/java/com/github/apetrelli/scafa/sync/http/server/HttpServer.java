package com.github.apetrelli.scafa.sync.http.server;

import java.io.InputStream;
import java.nio.file.Path;

import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.proto.io.FlowBuffer;
import com.github.apetrelli.scafa.sync.http.HttpSyncSocket;

public interface HttpServer {

	void response(HttpSyncSocket<HttpResponse> channel, HttpResponse response);

	void response(HttpSyncSocket<HttpResponse> channel, HttpResponse response, InputStream payload, FlowBuffer writeBuffer);

	void response(HttpSyncSocket<HttpResponse> channel, HttpResponse response, InputStream payload, long size, FlowBuffer writeBuffer);

	void response(HttpSyncSocket<HttpResponse> channel, HttpResponse response, Path payload, FlowBuffer writeBuffer);
}
