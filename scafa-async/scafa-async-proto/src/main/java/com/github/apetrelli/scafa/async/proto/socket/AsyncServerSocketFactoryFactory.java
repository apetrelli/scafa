package com.github.apetrelli.scafa.async.proto.socket;

public interface AsyncServerSocketFactoryFactory {

	AsyncServerSocketFactory<AsyncSocket> create(int portNumber, String interfaceName, boolean forceIpV4);
	
	default AsyncServerSocketFactory<AsyncSocket> create (int portNumber) {
		return create(portNumber, null, false);
	}
}
