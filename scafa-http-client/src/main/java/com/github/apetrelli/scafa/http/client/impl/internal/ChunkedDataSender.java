package com.github.apetrelli.scafa.http.client.impl.internal;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

import com.github.apetrelli.scafa.http.client.HttpClientConnection;

public class ChunkedDataSender implements DataSender {

	private HttpClientConnection connection;

	public ChunkedDataSender(HttpClientConnection connection) {
		this.connection = connection;
	}

	@Override
	public void send(ByteBuffer buffer, CompletionHandler<Void, Void> completionHandler) {
		connection.sendAsChunk(buffer, completionHandler);
	}

	@Override
	public void end(CompletionHandler<Void, Void> completionHandler) {
		connection.endChunkedTransfer(completionHandler);
	}
}
