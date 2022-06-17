package com.github.apetrelli.scafa.async.proto.netty;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import com.github.apetrelli.scafa.async.proto.socket.AsyncServerSocket;
import com.github.apetrelli.scafa.async.proto.socket.AsyncServerSocketFactory;
import com.github.apetrelli.scafa.async.proto.socket.AsyncSocket;
import com.github.apetrelli.scafa.proto.util.NetworkUtils;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.socket.ServerSocketChannel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DirectAsyncServerSocketFactory implements AsyncServerSocketFactory<AsyncSocket> {
	
	private final ServerBootstrap bootstrap;

    private final int portNumber;

    private final String interfaceName;

    private final boolean forceIpV4;
    
    public DirectAsyncServerSocketFactory(ServerBootstrap bootstrap, int portNumber) {
		this(bootstrap, portNumber, null, false);
	}

	@Override
	public AsyncServerSocket<AsyncSocket> create() throws IOException {
		ServerSocketContextHolder serverSocketContextHolder = new ServerSocketContextHolder();
		bootstrap.childHandler(new ServerSocketChannelInitializer(serverSocketContextHolder));
		InetSocketAddress socketAddress;
		if (interfaceName != null) {
			InetAddress address = NetworkUtils.getInterfaceAddress(interfaceName, forceIpV4);
	        if (address != null) {
	            socketAddress = new InetSocketAddress(address, portNumber);
	        } else {
	        	throw new IOException("Cannot find interface named " + interfaceName);
	        }
		} else {
			socketAddress = new InetSocketAddress(portNumber);
		}
		try {
			return new DirectAsyncServerSocket(serverSocketContextHolder, (ServerSocketChannel) bootstrap.bind(socketAddress).sync().channel());
		} catch (InterruptedException e) {
			throw new IOException(e);
		}
	}

}
