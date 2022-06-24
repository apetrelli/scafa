package com.github.apetrelli.scafa.async.proto.netty;

import com.github.apetrelli.scafa.async.proto.socket.AsyncServerSocketFactory;
import com.github.apetrelli.scafa.async.proto.socket.AsyncServerSocketFactoryFactory;
import com.github.apetrelli.scafa.async.proto.socket.AsyncSocket;

import io.netty.channel.EventLoopGroup;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NettyAsyncServerSocketFactoryFactory implements AsyncServerSocketFactoryFactory {
	
	private final EventLoopGroup bossGroup;
	
	private final EventLoopGroup workerGroup;
	
	@Override
	public AsyncServerSocketFactory<AsyncSocket> create(int portNumber, String interfaceName, boolean forceIpV4) {
		return new DirectAsyncServerSocketFactory(bossGroup, workerGroup, portNumber, interfaceName, forceIpV4);
	}

}
