package com.github.apetrelli.scafa.proto.aio.sync;

import java.net.Socket;

import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.aio.AsyncSocketFactory;
import com.github.apetrelli.scafa.proto.client.HostPort;

public class DirectClientSyncSocketFactory implements AsyncSocketFactory<AsyncSocket>{
	
	@Override
	public AsyncSocket create(HostPort hostPort, String interfaceName, boolean forceIpV4) {
		Socket channel = new Socket();
		return new DirectClientSyncSocket(channel, hostPort, interfaceName, forceIpV4);
	}

}
