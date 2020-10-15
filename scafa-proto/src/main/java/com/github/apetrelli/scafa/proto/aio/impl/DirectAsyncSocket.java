package com.github.apetrelli.scafa.proto.aio.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.aio.CompletionHandlerFuture;
import com.github.apetrelli.scafa.proto.aio.CompletionHandlerResult;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.tls.TlsConnectionException;

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
			throw new TlsConnectionException(e);
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
	public <A> CompletableFuture<CompletionHandlerResult<Integer, A>> read(ByteBuffer buffer, A attachment) {
		CompletableFuture<CompletionHandlerResult<Integer, A>> future = new CompletableFuture<>();
		channel.read(buffer, new CompletableFutureAttachmentPair<>(future, attachment), CompletableFutureAttachmentPairCompletionHandler.getInstance());
		return future;
	}
	
	@Override
	public <A> CompletableFuture<CompletionHandlerResult<Integer, A>> write(ByteBuffer buffer, A attachment) {
		CompletableFuture<CompletionHandlerResult<Integer, A>> future = new CompletableFuture<>();
		channel.write(buffer, new CompletableFutureAttachmentPair<>(future, attachment), CompletableFutureAttachmentPairCompletionHandler.getInstance());
		return future;
	}
}
