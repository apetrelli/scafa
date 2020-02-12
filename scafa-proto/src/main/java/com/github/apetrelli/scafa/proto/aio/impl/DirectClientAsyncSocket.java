package com.github.apetrelli.scafa.proto.aio.impl;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.channels.CompletionHandler;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.aio.AsynchronousSocketChannelFactory;
import com.github.apetrelli.scafa.proto.aio.DelegateFailureCompletionHandler;
import com.github.apetrelli.scafa.proto.client.HostPort;

public class DirectClientAsyncSocket extends DirectAsyncSocket implements AsyncSocket {
	
	private static final Logger LOG = Logger.getLogger(DirectClientAsyncSocket.class.getName());

	private HostPort socketAddress;

    private String interfaceName;

    private boolean forceIpV4;
    
	public DirectClientAsyncSocket(AsynchronousSocketChannelFactory channelFactory, HostPort socketAddress,
			String interfaceName, boolean forceIpV4) {
		super(channelFactory.create());
		this.socketAddress = socketAddress;
		this.interfaceName = interfaceName;
		this.forceIpV4 = forceIpV4;
	}

	@Override
	public HostPort getAddress() {
		return socketAddress;
	}
	
	@Override
	public void connect(CompletionHandler<Void, Void> handler) {
        if (LOG.isLoggable(Level.INFO)) {
            LOG.log(Level.INFO, "Connected thread {0} to address {1}",
                    new Object[] { Thread.currentThread().getName(), socketAddress.toString() });
        }
        try {
            bindChannel();
        } catch (IOException e1) {
            handler.failed(e1, null);
        }
        establishConnection(new DelegateFailureCompletionHandler<Void, Void>(handler) {

            @Override
            public void completed(Void result, Void attachment) {
                if (LOG.isLoggable(Level.INFO)) {
                    try {
                        LOG.log(Level.INFO, "Connected thread {0} to port {1}",
                                new Object[] { Thread.currentThread().getName(), channel.getLocalAddress().toString() });
                    } catch (IOException e) {
                        LOG.log(Level.SEVERE, "Cannot obtain local address", e);
                    }
                }

                handler.completed(result, attachment);
            }
        });
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

    protected void establishConnection(CompletionHandler<Void, Void> handler) {
        LOG.finest("Trying to connect to " + socketAddress.toString());
        channel.connect(new InetSocketAddress(socketAddress.getHost(), socketAddress.getPort()), null, handler);
    }

}
