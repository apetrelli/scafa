package com.github.apetrelli.scafa.async.http;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.async.proto.socket.AsyncSocket;
import com.github.apetrelli.scafa.http.HeaderHolder;

public interface HttpAsyncSocket<H extends HeaderHolder> extends AsyncSocket {

	CompletableFuture<Void> sendHeader(H holder, ByteBuffer buffer);
	
	CompletableFuture<Void> sendData(ByteBuffer buffer);

	CompletableFuture<Void> endData();
}
