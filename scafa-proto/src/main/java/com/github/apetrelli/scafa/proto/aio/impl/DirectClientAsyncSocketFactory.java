package com.github.apetrelli.scafa.proto.aio.impl;

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;

import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.aio.AsyncSocketFactory;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.tls.TlsConnectionException;

public class DirectClientAsyncSocketFactory implements AsyncSocketFactory<AsyncSocket>{
	
	@Override
	public AsyncSocket create(HostPort hostPort, String interfaceName, boolean forceIpV4) {
		try {
			AsynchronousSocketChannel channel = AsynchronousSocketChannel.open();
			return new DirectClientAsyncSocket(channel, hostPort, interfaceName, forceIpV4);
		} catch (IOException e) {
			throw new TlsConnectionException(e);
		}
	}

}
