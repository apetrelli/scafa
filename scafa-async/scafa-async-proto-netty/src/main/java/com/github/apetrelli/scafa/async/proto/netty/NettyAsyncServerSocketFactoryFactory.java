package com.github.apetrelli.scafa.async.proto.netty;

import com.github.apetrelli.scafa.async.proto.socket.AsyncServerSocketFactory;
import com.github.apetrelli.scafa.async.proto.socket.AsyncServerSocketFactoryFactory;
import com.github.apetrelli.scafa.async.proto.socket.AsyncSocket;

import io.netty.bootstrap.ServerBootstrap;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NettyAsyncServerSocketFactoryFactory implements AsyncServerSocketFactoryFactory {

	private final ServerBootstrap bootstrap;
	
	@Override
	public AsyncServerSocketFactory<AsyncSocket> create(int portNumber, String interfaceName, boolean forceIpV4) {
		return new DirectAsyncServerSocketFactory(bootstrap, portNumber, interfaceName, forceIpV4);
	}

}
