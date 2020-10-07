package com.github.apetrelli.scafa.proto.aio;

import java.net.Socket;

public interface AsyncSocketFactory<T extends AsyncSocket> {

	T create(Socket channel);
}
