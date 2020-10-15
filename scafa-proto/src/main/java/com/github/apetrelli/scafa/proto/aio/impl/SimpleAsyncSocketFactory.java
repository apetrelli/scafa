package com.github.apetrelli.scafa.proto.aio.impl;

import java.nio.channels.AsynchronousSocketChannel;

import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.aio.AsyncSocketFactory;

public class SimpleAsyncSocketFactory implements AsyncSocketFactory<AsyncSocket> {

	@Override
	public AsyncSocket create(AsynchronousSocketChannel channel) {
		return new DirectAsyncSocket(channel);
	}

}
