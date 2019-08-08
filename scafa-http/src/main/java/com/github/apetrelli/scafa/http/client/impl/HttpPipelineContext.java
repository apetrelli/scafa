package com.github.apetrelli.scafa.http.client.impl;

import com.github.apetrelli.scafa.http.HttpConnection;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.client.HttpClientHandler;

public class HttpPipelineContext {

	private HttpClientHandler handler;

	private HttpRequest request;

	private HttpResponse response;

	private HttpConnection connection;

	public HttpPipelineContext(HttpClientHandler handler, HttpRequest request) {
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

	public HttpConnection getConnection() {
		return connection;
	}

	public void setConnection(HttpConnection connection) {
		this.connection = connection;
	}


}
