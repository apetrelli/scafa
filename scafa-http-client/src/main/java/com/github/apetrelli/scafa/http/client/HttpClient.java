package com.github.apetrelli.scafa.http.client;

import java.nio.file.Path;

import com.github.apetrelli.scafa.async.proto.buffer.BufferContextReader;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.client.impl.DefaultHttpClient;

public interface HttpClient {

	static HttpClient DEFAULT_INSTANCE = new DefaultHttpClient();

	public static HttpClient getDefault() {
		return DEFAULT_INSTANCE;
	}

	void request(HttpRequest request, HttpClientHandler handler);

	void request(HttpRequest request, BufferContextReader payloadReader, HttpClientHandler handler);

	void request(HttpRequest request, BufferContextReader payloadReader, long size, HttpClientHandler handler);

	void request(HttpRequest request, Path payload, HttpClientHandler handler);
}
