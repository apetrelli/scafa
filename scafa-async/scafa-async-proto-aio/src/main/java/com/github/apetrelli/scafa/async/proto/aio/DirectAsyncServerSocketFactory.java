package com.github.apetrelli.scafa.async.proto.aio;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;

import com.github.apetrelli.scafa.async.proto.socket.AsyncServerSocket;
import com.github.apetrelli.scafa.async.proto.socket.AsyncServerSocketFactory;
import com.github.apetrelli.scafa.async.proto.socket.AsyncSocket;
import com.github.apetrelli.scafa.proto.util.NetworkUtils;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DirectAsyncServerSocketFactory implements AsyncServerSocketFactory<AsyncSocket> {

    private final int portNumber;

    private final String interfaceName;

    private final boolean forceIpV4;
	
	public DirectAsyncServerSocketFactory(int portNumber) {
		this(portNumber, null, false);
	}

	@Override
	public AsyncServerSocket<AsyncSocket> create() throws IOException {
    	@SuppressWarnings("resource")
		AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open().bind(new InetSocketAddress(portNumber)); // NOSONAR
        bindChannel(server);
        return new DirectAsyncServerSocket(server);
	}

    private void bindChannel(AsynchronousServerSocketChannel server) throws IOException {
		InetAddress address = NetworkUtils.getInterfaceAddress(interfaceName, forceIpV4);
        if (address != null) {
            server.bind(new InetSocketAddress(address, 0));
        }
    }

}
