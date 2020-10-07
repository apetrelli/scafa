package com.github.apetrelli.scafa.proto.aio.impl;

import java.net.Socket;

import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.aio.AsyncSocketFactory;

public class SimpleAsyncSocketFactory implements AsyncSocketFactory<AsyncSocket> {

	@Override
	public AsyncSocket create(Socket channel) {
		return new DirectAsyncSocket(channel);
	}

}
