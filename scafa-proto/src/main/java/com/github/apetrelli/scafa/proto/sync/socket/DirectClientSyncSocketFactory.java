package com.github.apetrelli.scafa.proto.sync.socket;

import java.net.Socket;

import com.github.apetrelli.scafa.proto.aio.SocketFactory;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.sync.SyncSocket;

public class DirectClientSyncSocketFactory implements SocketFactory<SyncSocket>{
	
	@Override
	public SyncSocket create(HostPort hostPort, String interfaceName, boolean forceIpV4) {
		Socket channel = new Socket();
		return new DirectClientSyncSocket(channel, hostPort, interfaceName, forceIpV4);
	}

}
