package com.github.apetrelli.scafa.http.client.impl;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.client.HttpClientHandler;

public class HttpPipelineContext {

	private HttpClientHandler handler;

	private HttpRequest request;

	private HttpResponse response;

	public HttpPipelineContext(HttpRequest request, HttpClientHandler handler) {
		this.handler = handler;
		this.request = request;
	}

	public HttpClientHandler getHandler() {
		return handler;
	}

	public HttpRequest getRequest() {
		return request;
	}

	public HttpResponse getResponse() {
		return response;
	}

	public void setResponse(HttpResponse response) {
		this.response = response;
	}
}
