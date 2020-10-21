package com.github.apetrelli.scafa.proto.aio;

import com.github.apetrelli.scafa.proto.client.HostPort;

public interface AsyncSocketFactory<T extends AsyncSocket> {

	T create(HostPort hostPort, String interfaceName, boolean forceIpV4);
}
