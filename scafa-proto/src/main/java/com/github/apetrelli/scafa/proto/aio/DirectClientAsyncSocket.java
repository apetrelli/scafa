package com.github.apetrelli.scafa.proto.aio;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.proto.async.socket.AsyncSocket;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.util.NetworkUtils;

public class DirectClientAsyncSocket extends DirectAsyncSocket implements AsyncSocket {
	
	private static final Logger LOG = Logger.getLogger(DirectClientAsyncSocket.class.getName());

	private HostPort socketAddress;

    private String interfaceName;

    private boolean forceIpV4;
    
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
        if (LOG.isLoggable(Level.INFO)) {
            LOG.log(Level.INFO, "Connected thread {0} to address {1}",
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

    protected CompletableFuture<Void> establishConnection() {
        LOG.log(Level.FINEST, "Trying to connect to {0}", socketAddress);
        CompletableFuture<Void> future = new CompletableFuture<>();
        channel.connect(new InetSocketAddress(socketAddress.getHost(), socketAddress.getPort()), future, CompleterCompletionHandler.INSTANCE);
        return future;
    }

}
