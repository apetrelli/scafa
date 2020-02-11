package com.github.apetrelli.scafa.proto.aio;

import java.nio.channels.CompletionHandler;

import com.github.apetrelli.scafa.proto.client.HostPort;

public interface ClientAsyncSocket extends AsyncSocket {

	HostPort getAddress();
	
	void connect(CompletionHandler<Void, Void> handler);
}
