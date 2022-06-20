package com.github.apetrelli.scafa.async.proto.aio;

import com.github.apetrelli.scafa.async.proto.socket.AsyncServerSocketFactory;
import com.github.apetrelli.scafa.async.proto.socket.AsyncServerSocketFactoryFactory;
import com.github.apetrelli.scafa.async.proto.socket.AsyncSocket;

public class AioAsyncServerSocketFactoryFactory implements AsyncServerSocketFactoryFactory {

	@Override
	public AsyncServerSocketFactory<AsyncSocket> create(int portNumber, String interfaceName, boolean forceIpV4) {
		return new DirectAsyncServerSocketFactory(portNumber, interfaceName, forceIpV4);
	}

}
