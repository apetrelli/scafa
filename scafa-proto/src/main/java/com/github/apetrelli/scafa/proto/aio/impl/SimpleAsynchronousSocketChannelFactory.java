package com.github.apetrelli.scafa.proto.aio.impl;

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;

import com.github.apetrelli.scafa.proto.aio.AsynchronousSocketChannelFactory;
import com.github.apetrelli.scafa.tls.TlsConnectionException;

public class SimpleAsynchronousSocketChannelFactory implements AsynchronousSocketChannelFactory {

	@Override
	public AsynchronousSocketChannel create() {
		try {
			return AsynchronousSocketChannel.open();
		} catch (IOException e) {
			throw new TlsConnectionException(e);
		}
	}

}
