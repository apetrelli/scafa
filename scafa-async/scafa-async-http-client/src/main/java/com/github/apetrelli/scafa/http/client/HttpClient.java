package com.github.apetrelli.scafa.http.client;

import java.nio.file.Path;

import com.github.apetrelli.scafa.async.file.BufferContextReader;
import com.github.apetrelli.scafa.http.HttpRequest;

public interface HttpClient {

	void request(HttpRequest request, HttpClientHandler handler);

	void request(HttpRequest request, BufferContextReader payloadReader, HttpClientHandler handler);

	void request(HttpRequest request, BufferContextReader payloadReader, long size, HttpClientHandler handler);

	void request(HttpRequest request, Path payload, HttpClientHandler handler);
}
