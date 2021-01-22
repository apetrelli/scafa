package com.github.apetrelli.scafa.proto.sync.socket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.proto.IORuntimeException;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.util.NetworkUtils;

public class DirectClientSyncSocket extends DirectSyncSocket {
	
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
	public void connect() {
        if (LOG.isLoggable(Level.INFO)) {
            LOG.log(Level.INFO, "Connected thread {0} to address {1}",
                    new Object[] { Thread.currentThread().getName(), socketAddress});
        }
        try {
            bindChannel();
        } catch (IOException e1) {
            throw new IORuntimeException(e1);
        }
        establishConnection();
        super.connect();
	}

	private void bindChannel() throws IOException {
		InetAddress address = NetworkUtils.getInterfaceAddress(interfaceName, forceIpV4);
        if (address != null) {
            channel.bind(new InetSocketAddress(address, 0));
        }
	}

    protected void establishConnection() {
        LOG.log(Level.FINEST, "Trying to connect to {0}", socketAddress);
        try {
			channel.connect(new InetSocketAddress(socketAddress.getHost(), socketAddress.getPort()));
		} catch (IOException e) {
            throw new IORuntimeException(e);
		}
    }

}
