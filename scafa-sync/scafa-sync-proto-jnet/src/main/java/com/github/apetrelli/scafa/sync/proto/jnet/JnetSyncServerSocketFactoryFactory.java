package com.github.apetrelli.scafa.sync.proto.jnet;

import com.github.apetrelli.scafa.sync.proto.SyncServerSocketFactory;
import com.github.apetrelli.scafa.sync.proto.SyncServerSocketFactoryFactory;
import com.github.apetrelli.scafa.sync.proto.SyncSocket;

public class JnetSyncServerSocketFactoryFactory implements SyncServerSocketFactoryFactory {

	@Override
	public SyncServerSocketFactory<SyncSocket> create(int portNumber, String interfaceName, boolean forceIpV4) {
		return new DirectSyncServerSocketFactory(portNumber, interfaceName, forceIpV4);
	}

}
