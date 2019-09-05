package com.github.apetrelli.scafa.http.client.impl.internal;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

import com.github.apetrelli.scafa.http.client.HttpClientConnection;

public class DirectDataSender implements DataSender {

	private HttpClientConnection connection;

	public DirectDataSender(HttpClientConnection connection) {
		this.connection = connection;
	}

	@Override
	public void send(ByteBuffer buffer, CompletionHandler<Void, Void> completionHandler) {
		connection.send(buffer, completionHandler);
	}

	@Override
	public void end(CompletionHandler<Void, Void> completionHandler) {
		completionHandler.completed(null, null);
	}
}
