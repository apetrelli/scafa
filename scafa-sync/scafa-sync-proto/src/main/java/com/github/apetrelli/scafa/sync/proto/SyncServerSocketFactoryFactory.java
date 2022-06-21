package com.github.apetrelli.scafa.sync.proto;

public interface SyncServerSocketFactoryFactory {

	SyncServerSocketFactory<SyncSocket> create(int portNumber, String interfaceName, boolean forceIpV4);
	
	default SyncServerSocketFactory<SyncSocket> create (int portNumber) {
		return create(portNumber, null, false);
	}
}
