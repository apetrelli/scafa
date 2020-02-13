package com.github.apetrelli.scafa.http;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

import com.github.apetrelli.scafa.proto.aio.AsyncSocket;

public interface HttpAsyncSocket<H extends HeaderHolder> extends AsyncSocket {

	void sendHeader(H header, CompletionHandler<Void, Void> completionHandler);
	
	void sendData(ByteBuffer buffer, CompletionHandler<Void, Void> completionHandler);

	void endData(CompletionHandler<Void, Void> completionHandler);
}
