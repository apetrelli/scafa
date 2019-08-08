package com.github.apetrelli.scafa.http.client.impl;

import java.nio.channels.CompletionHandler;

import com.github.apetrelli.scafa.http.HttpConnection;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.client.HttpClient;
import com.github.apetrelli.scafa.http.client.HttpClientHandler;
import com.github.apetrelli.scafa.proto.aio.ResultHandler;

public class DefaultHttpClient implements HttpClient {

	private ClientPipelineHttpHandler mainHandler;

	private MappedHttpConnectionFactory connectionFactory;

	public DefaultHttpClient() {
		connectionFactory = new DefaultMappedHttpConnectionFactory();
		mainHandler = new ClientPipelineHttpHandler();
	}

	@Override
	public void request(HttpRequest request, HttpClientHandler handler) {
		mainHandler.add(handler, request);
		connectionFactory.create(request, new ResultHandler<HttpConnection>() {

			@Override
			public void handle(HttpConnection result) {
				mainHandler.setConnection(result);
				result.sendHeader(request, new CompletionHandler<Void, Void>() {

					@Override
					public void completed(Void result, Void attachment) {
						handler.onRequestHeaderSent(request);
					}

					@Override
					public void failed(Throwable exc, Void attachment) {
						handler.onRequestError(exc);
					}
				});
			}
		}, mainHandler);
	}

}
