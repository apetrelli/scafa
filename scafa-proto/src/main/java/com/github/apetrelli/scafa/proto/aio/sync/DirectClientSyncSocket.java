package com.github.apetrelli.scafa.proto.aio.sync;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.aio.CompletionHandlerFuture;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.util.NetworkUtils;

public class DirectClientSyncSocket extends DirectSyncSocket implements AsyncSocket {
	
	private static final Logger LOG = Logger.getLogger(DirectClientSyncSocket.class.getName());

	private HostPort socketAddress;

    private String interfaceName;

    private boolean forceIpV4;
    
	public DirectClientSyncSocket(Socket channel, HostPort socketAddress,
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
        try {
			channel.connect(new InetSocketAddress(socketAddress.getHost(), socketAddress.getPort()));
			return CompletionHandlerFuture.completeEmpty();
		} catch (IOException e) {
			return CompletableFuture.failedFuture(e);
		}
    }

}
