package com.github.apetrelli.scafa.sync.proto.jnet;

import java.net.Socket;

import com.github.apetrelli.scafa.proto.SocketFactory;
import com.github.apetrelli.scafa.proto.client.HostPort;

public class DirectClientSyncSocketFactory implements SocketFactory<com.github.apetrelli.scafa.proto.Socket>{
	
	@Override
	public com.github.apetrelli.scafa.proto.Socket create(HostPort hostPort, String interfaceName, boolean forceIpV4) {
		Socket channel = new Socket();
		return new DirectClientSyncSocket(channel, hostPort, interfaceName, forceIpV4);
	}

}
