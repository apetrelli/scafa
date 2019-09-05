package com.github.apetrelli.scafa.http.client.impl.internal;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

public interface DataSender {

	void send(ByteBuffer buffer, CompletionHandler<Void, Void> completionHandler);

	void end(CompletionHandler<Void, Void> completionHandler);
}
