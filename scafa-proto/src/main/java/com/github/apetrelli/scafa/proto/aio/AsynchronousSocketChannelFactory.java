package com.github.apetrelli.scafa.proto.aio;

import java.nio.channels.AsynchronousSocketChannel;

public interface AsynchronousSocketChannelFactory {

	AsynchronousSocketChannel create();
}
