package com.github.apetrelli.scafa.async.proto.netty;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.util.NetworkUtils;

import io.netty.channel.socket.SocketChannel;
import lombok.extern.java.Log;

@Log
public class DirectClientAsyncSocket extends CoalescingAsyncSocket {

	private final HostPort socketAddress;

    private final String interfaceName;

    private final boolean forceIpV4;

	public DirectClientAsyncSocket(SocketChannel channel, HostPort socketAddress,
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
    		InetAddress address = NetworkUtils.getInterfaceAddress(interfaceName, forceIpV4);
            CompletableFuture<Void> future = new CompletableFuture<>();
            if (address != null) {
                channel.bind(new InetSocketAddress(address, 0)).addListener(f -> {
                	if (f.isSuccess()) {
                		doConnect(future);
                	} else {
                		future.completeExceptionally(f.cause());
                	}
                });
            } else {
            	doConnect(future);
            }
            return future;
        } catch (IOException e1) {
            return CompletableFuture.failedFuture(e1);
        }
	}

	private void doConnect(CompletableFuture<Void> future) {
		channel.connect(new InetSocketAddress(socketAddress.getHost(), socketAddress.getPort())).addListener(f -> {
        	if (f.isSuccess()) {
        		future.complete(null);
        	} else {
        		future.completeExceptionally(f.cause());
        	}
        });
	}
}
