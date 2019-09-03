package com.github.apetrelli.scafa.http.client;

import java.nio.file.Path;

import com.github.apetrelli.scafa.http.HttpRequest;

public interface HttpClient {

	void request(HttpRequest request, HttpClientHandler handler);

	void request(HttpRequest request, Path payload, HttpClientHandler handler);
}
