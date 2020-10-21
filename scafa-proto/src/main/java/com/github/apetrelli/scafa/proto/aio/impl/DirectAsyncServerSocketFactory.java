package com.github.apetrelli.scafa.proto.aio.impl;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.Enumeration;

import com.github.apetrelli.scafa.proto.aio.AsyncServerSocket;
import com.github.apetrelli.scafa.proto.aio.AsyncServerSocketFactory;
import com.github.apetrelli.scafa.proto.aio.AsyncSocket;

public class DirectAsyncServerSocketFactory implements AsyncServerSocketFactory<AsyncSocket> {

    private final int portNumber;

    private final String interfaceName;

    private final boolean forceIpV4;
    
	public DirectAsyncServerSocketFactory(int portNumber, String interfaceName, boolean forceIpV4) {
		this.portNumber = portNumber;
		this.interfaceName = interfaceName;
		this.forceIpV4 = forceIpV4;
	}
	
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
            server.bind(new InetSocketAddress(address, 0));
        }
    }

}
