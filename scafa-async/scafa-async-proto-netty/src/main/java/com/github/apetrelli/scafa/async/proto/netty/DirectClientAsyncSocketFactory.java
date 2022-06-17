package com.github.apetrelli.scafa.async.proto.netty;

import com.github.apetrelli.scafa.async.proto.socket.AsyncSocket;
import com.github.apetrelli.scafa.proto.SocketFactory;
import com.github.apetrelli.scafa.proto.client.HostPort;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.socket.SocketChannel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DirectClientAsyncSocketFactory implements SocketFactory<AsyncSocket> {
	
	private final Bootstrap bootstrap;

	@Override
	public AsyncSocket create(HostPort hostPort, String interfaceName, boolean forceIpV4) {
		return new DirectClientAsyncSocket((SocketChannel) bootstrap.register().channel(), hostPort, interfaceName, forceIpV4);
	}

}
