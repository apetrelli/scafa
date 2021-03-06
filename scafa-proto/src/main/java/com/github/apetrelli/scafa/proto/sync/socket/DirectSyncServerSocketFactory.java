package com.github.apetrelli.scafa.proto.sync.socket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

import com.github.apetrelli.scafa.proto.sync.SyncServerSocket;
import com.github.apetrelli.scafa.proto.sync.SyncServerSocketFactory;
import com.github.apetrelli.scafa.proto.sync.SyncSocket;
import com.github.apetrelli.scafa.proto.util.NetworkUtils;

public class DirectSyncServerSocketFactory implements SyncServerSocketFactory<SyncSocket> {

    private final int portNumber;

    private final String interfaceName;

    private final boolean forceIpV4;
    
	public DirectSyncServerSocketFactory(int portNumber, String interfaceName, boolean forceIpV4) {
		this.portNumber = portNumber;
		this.interfaceName = interfaceName;
		this.forceIpV4 = forceIpV4;
	}
	
	public DirectSyncServerSocketFactory(int portNumber) {
		this(portNumber, null, false);
	}

	@Override
	public SyncServerSocket<SyncSocket> create() throws IOException {
		ServerSocket server = new ServerSocket(portNumber);
        bindChannel(server);
        return new DirectSyncServerSocket(server);
	}

    private void bindChannel(ServerSocket server) throws IOException {
		InetAddress address = NetworkUtils.getInterfaceAddress(interfaceName, forceIpV4);
        if (address != null) {
            server.bind(new InetSocketAddress(address, 0));
        }
    }

}
