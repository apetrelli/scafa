package com.github.apetrelli.scafa.proto.aio;

import com.github.apetrelli.scafa.proto.client.HostPort;

public interface SocketFactory<T> {

	T create(HostPort hostPort, String interfaceName, boolean forceIpV4);
}
