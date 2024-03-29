package com.github.apetrelli.scafa.sync.proto.jnet;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

import com.github.apetrelli.scafa.proto.util.NetworkUtils;
import com.github.apetrelli.scafa.sync.proto.SyncServerSocket;
import com.github.apetrelli.scafa.sync.proto.SyncServerSocketFactory;
import com.github.apetrelli.scafa.sync.proto.SyncSocket;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DirectSyncServerSocketFactory implements SyncServerSocketFactory<SyncSocket> {

    private final int portNumber;

    private final String interfaceName;

    private final boolean forceIpV4;
	
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
