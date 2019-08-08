package com.github.apetrelli.scafa.http.client.impl;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.client.HttpClient;
import com.github.apetrelli.scafa.http.client.HttpClientConnection;
import com.github.apetrelli.scafa.http.client.HttpClientHandler;
import com.github.apetrelli.scafa.proto.aio.ResultHandler;

public class DefaultHttpClient implements HttpClient {

	private MappedHttpConnectionFactory connectionFactory;

	public DefaultHttpClient() {
		connectionFactory = new DefaultMappedHttpConnectionFactory();
	}

	@Override
	public void request(HttpRequest request, HttpClientHandler handler) {
		connectionFactory.create(request, new ResultHandler<HttpClientConnection>() {

			@Override
			public void handle(HttpClientConnection result) {
				result.sendHeader(request, handler);
			}
		});
	}

}
