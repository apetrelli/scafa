package com.github.apetrelli.scafa.sync.proto;

import com.github.apetrelli.scafa.proto.Socket;

public interface SyncServerSocketFactoryFactory {

	SyncServerSocketFactory<Socket> create(int portNumber, String interfaceName, boolean forceIpV4);
	
	default SyncServerSocketFactory<Socket> create (int portNumber) {
		return create(portNumber, null, false);
	}
}
