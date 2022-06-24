package com.github.apetrelli.scafa.async.proto.netty;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import com.github.apetrelli.scafa.async.proto.socket.AsyncServerSocket;
import com.github.apetrelli.scafa.async.proto.socket.AsyncServerSocketFactory;
import com.github.apetrelli.scafa.async.proto.socket.AsyncSocket;
import com.github.apetrelli.scafa.proto.util.NetworkUtils;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class DirectAsyncServerSocketFactory implements AsyncServerSocketFactory<AsyncSocket> {
	
	private final ServerBootstrap bootstrap;

	private final SocketQueueManager socketQueueManager;

    private final int portNumber;

    private final String interfaceName;

    private final boolean forceIpV4;

	public DirectAsyncServerSocketFactory(EventLoopGroup bossGroup, EventLoopGroup workerGroup, int portNumber,
			String interfaceName, boolean forceIpV4) {
        bootstrap = new ServerBootstrap();
		socketQueueManager = new SocketQueueManager();
		bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
				.option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE, true)
				.childOption(ChannelOption.AUTO_READ, true).childOption(ChannelOption.AUTO_CLOSE, false)
				.childHandler(new ServerSocketChannelInitializer(socketQueueManager));
		this.portNumber = portNumber;
		this.interfaceName = interfaceName;
		this.forceIpV4 = forceIpV4;
	}
    
    public DirectAsyncServerSocketFactory(EventLoopGroup bossGroup, EventLoopGroup workerGroup, int portNumber) {
		this(bossGroup, workerGroup, portNumber, null, false);
	}
    
    

	@Override
	public AsyncServerSocket<AsyncSocket> create() throws IOException {
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
			return new DirectAsyncServerSocket(socketQueueManager, (ServerSocketChannel) bootstrap.bind(socketAddress).sync().channel());
		} catch (InterruptedException e) {
			throw new IOException(e);
		}
	}



}
