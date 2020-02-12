package com.github.apetrelli.scafa.proto.aio;

import java.nio.channels.AsynchronousSocketChannel;

public interface AsyncSocketFactory<T extends AsyncSocket> {

	T create(AsynchronousSocketChannel channel);
}
