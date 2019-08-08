package com.github.apetrelli.scafa.http.client;

import com.github.apetrelli.scafa.http.HttpRequest;

public interface HttpClient {

	void request(HttpRequest request, HttpClientHandler handler);
}
