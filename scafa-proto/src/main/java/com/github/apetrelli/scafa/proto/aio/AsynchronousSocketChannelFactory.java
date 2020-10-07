package com.github.apetrelli.scafa.proto.aio;

import java.net.Socket;

public interface AsynchronousSocketChannelFactory {

	Socket create();
}
