package com.github.apetrelli.scafa.proto.aio.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.proto.IORuntimeException;
import com.github.apetrelli.scafa.proto.async.AsyncSocket;
import com.github.apetrelli.scafa.proto.async.util.CompletionHandlerFuture;
import com.github.apetrelli.scafa.proto.client.HostPort;

public class DirectAsyncSocket implements AsyncSocket {
	
	protected AsynchronousSocketChannel channel;
    
	public DirectAsyncSocket(AsynchronousSocketChannel channel) {
		this.channel = channel;
	}
	
	@Override
	public CompletableFuture<Void> connect() {
		return CompletionHandlerFuture.completeEmpty();
	}
	
	@Override
	public HostPort getAddress() {
		HostPort retValue = null;
		SocketAddress address;
		try {
			address = channel.getLocalAddress();
			if (address instanceof InetSocketAddress) {
				InetSocketAddress realAddress = (InetSocketAddress) address;
				retValue = new HostPort(realAddress.getHostName(), realAddress.getPort());
			}
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
		return retValue;
	}
	
	@Override
	public CompletableFuture<Void> disconnect() {
        if (channel != null && channel.isOpen()) {
    		try {
    			channel.close();
    			return CompletionHandlerFuture.completeEmpty();
    		} catch (IOException e) {
    			return CompletableFuture.failedFuture(e);
    		}
        } else {
			return CompletionHandlerFuture.completeEmpty();
        }
	}
	
	@Override
	public boolean isOpen() {
		return channel.isOpen();
	}
	
	@Override
	public CompletableFuture<Integer> read(ByteBuffer buffer) {
		CompletableFuture<Integer> future = new CompletableFuture<>();
		channel.read(buffer, future, CompletableFutureCompletionHandler.getInstance());
		return future;
	}
	
	@Override
	public CompletableFuture<Integer> write(ByteBuffer buffer) {
		CompletableFuture<Integer> future = new CompletableFuture<>();
		channel.write(buffer, future, CompletableFutureCompletionHandler.getInstance());
		return future;
	}
}
