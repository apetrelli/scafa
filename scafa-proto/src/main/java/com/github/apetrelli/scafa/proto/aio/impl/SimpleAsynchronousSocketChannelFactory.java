package com.github.apetrelli.scafa.proto.aio.impl;

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;

import com.github.apetrelli.scafa.proto.aio.AsynchronousSocketChannelFactory;

public class SimpleAsynchronousSocketChannelFactory implements AsynchronousSocketChannelFactory {

	@Override
	public AsynchronousSocketChannel create() throws IOException {
		return AsynchronousSocketChannel.open();
	}

}
