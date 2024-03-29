package com.github.apetrelli.scafa.async.proto.aio;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.util.NetworkUtils;

import lombok.extern.java.Log;

@Log
public class DirectClientAsyncSocket extends DirectAsyncSocket {

	private final HostPort socketAddress;

    private final String interfaceName;

    private final boolean forceIpV4;
    
	public DirectClientAsyncSocket(AsynchronousSocketChannel channel, HostPort socketAddress,
			String interfaceName, boolean forceIpV4) {
		super(channel);
		this.socketAddress = socketAddress;
		this.interfaceName = interfaceName;
		this.forceIpV4 = forceIpV4;
	}

	@Override
	public HostPort getAddress() {
		return socketAddress;
	}
	
	@Override
	public CompletableFuture<Void> connect() {
        if (log.isLoggable(Level.INFO)) {
            log.log(Level.INFO, "Connected thread {0} to address {1}",
                    new Object[] { Thread.currentThread().getName(), socketAddress});
        }
        try {
            bindChannel();
        } catch (IOException e1) {
            return CompletableFuture.failedFuture(e1);
        }
        return establishConnection();
	}

	private void bindChannel() throws IOException {
		InetAddress address = NetworkUtils.getInterfaceAddress(interfaceName, forceIpV4);
        if (address != null) {
            channel.bind(new InetSocketAddress(address, 0));
        }
	}

    private CompletableFuture<Void> establishConnection() {
        log.log(Level.FINEST, "Trying to connect to {0}", socketAddress);
        CompletableFuture<Void> future = new CompletableFuture<>();
        channel.connect(new InetSocketAddress(socketAddress.getHost(), socketAddress.getPort()), future, CompleterCompletionHandler.INSTANCE);
        return future;
    }

}
