package com.github.apetrelli.scafa.http;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.proto.aio.AsyncSocket;

public interface HttpAsyncSocket<H extends HeaderHolder> extends AsyncSocket {

	CompletableFuture<Void> sendHeader(H holder);
	
	CompletableFuture<Void> sendData(ByteBuffer buffer);

	CompletableFuture<Void> endData();
}
