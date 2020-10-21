package com.github.apetrelli.scafa.proto.aio.impl;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Enumeration;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.client.HostPort;

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
        if (interfaceName != null) {
            NetworkInterface intf = NetworkInterface.getByName(interfaceName);
            if (!intf.isUp()) {
                throw new SocketException("The interface " + interfaceName + " is not connected");
            }
            Enumeration<InetAddress> addresses = intf.getInetAddresses();
            if (!addresses.hasMoreElements()) {
                throw new SocketException("The interface " + interfaceName + " has no addresses");
            }
            InetAddress address = null;
            while (addresses.hasMoreElements() && address == null) {
                InetAddress currentAddress = addresses.nextElement();
                if (!forceIpV4 || currentAddress instanceof Inet4Address) {
                    address = currentAddress;
                }
            }
            if (address == null) {
                throw new SocketException("Not able to find a feasible address for interface " + interfaceName);
            }
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
