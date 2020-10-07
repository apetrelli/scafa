package com.github.apetrelli.scafa.proto.aio.impl;

import java.net.Socket;

import com.github.apetrelli.scafa.proto.aio.AsynchronousSocketChannelFactory;

public class SimpleAsynchronousSocketChannelFactory implements AsynchronousSocketChannelFactory {

	@Override
	public Socket create() {
		return new Socket();
	}

}
