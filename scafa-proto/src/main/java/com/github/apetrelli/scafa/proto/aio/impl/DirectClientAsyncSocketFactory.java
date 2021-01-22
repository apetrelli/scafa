package com.github.apetrelli.scafa.proto.aio.impl;

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;

import com.github.apetrelli.scafa.proto.IORuntimeException;
import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.aio.SocketFactory;
import com.github.apetrelli.scafa.proto.client.HostPort;

public class DirectClientAsyncSocketFactory implements SocketFactory<AsyncSocket>{
	
	@Override
	public AsyncSocket create(HostPort hostPort, String interfaceName, boolean forceIpV4) {
		try {
			AsynchronousSocketChannel channel = AsynchronousSocketChannel.open();
			return new DirectClientAsyncSocket(channel, hostPort, interfaceName, forceIpV4);
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}

}
