package com.github.apetrelli.scafa.sync.proto.jnet;

import com.github.apetrelli.scafa.proto.Socket;
import com.github.apetrelli.scafa.sync.proto.SyncServerSocketFactory;
import com.github.apetrelli.scafa.sync.proto.SyncServerSocketFactoryFactory;

public class JnetSyncServerSocketFactoryFactory implements SyncServerSocketFactoryFactory {

	@Override
	public SyncServerSocketFactory<Socket> create(int portNumber, String interfaceName, boolean forceIpV4) {
		return new DirectSyncServerSocketFactory(portNumber, interfaceName, forceIpV4);
	}

}
