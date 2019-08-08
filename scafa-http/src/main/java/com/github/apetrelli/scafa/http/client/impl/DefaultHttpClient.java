package com.github.apetrelli.scafa.http.client.impl;

import java.nio.channels.CompletionHandler;

import com.github.apetrelli.scafa.http.HttpConnection;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.client.HttpClient;
import com.github.apetrelli.scafa.http.client.HttpClientHandler;
import com.github.apetrelli.scafa.proto.aio.ResultHandler;

public class DefaultHttpClient implements HttpClient {

	private ClientPipelineHttpHandler mainHandler = new ClientPipelineHttpHandler();

	private MappedHttpConnectionFactory connectionFactory = new DefaultMappedHttpConnectionFactory();

	@Override
	public void request(HttpRequest request, HttpClientHandler handler) {
		mainHandler.add(handler);
		connectionFactory.create(request, new ResultHandler<HttpConnection>() {

			@Override
			public void handle(HttpConnection result) {
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
