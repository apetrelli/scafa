package com.github.apetrelli.scafa.async.proto.netty;

import java.util.concurrent.CompletableFuture;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ServerSocketChannelInitializer extends ChannelInitializer<SocketChannel> {
	
	private final ServerSocketContextHolder serverSocketContextHolder;

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		serverSocketContextHolder.getCompletableFutureForAccept().complete(new DirectAsyncSocket(ch));
		serverSocketContextHolder.setCompletableFutureForAccept(new CompletableFuture<>());
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		serverSocketContextHolder.getCompletableFutureForAccept().completeExceptionally(cause);
		serverSocketContextHolder.setCompletableFutureForAccept(new CompletableFuture<>());
	}

}
