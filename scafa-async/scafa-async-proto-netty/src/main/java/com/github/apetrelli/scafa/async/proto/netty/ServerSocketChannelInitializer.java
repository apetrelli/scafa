package com.github.apetrelli.scafa.async.proto.netty;

import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.async.proto.socket.AsyncSocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ServerSocketChannelInitializer extends ChannelInitializer<SocketChannel> {
	
	private final ServerSocketContextHolder serverSocketContextHolder;

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		CompletableFuture<AsyncSocket> previousCompletableFuture = serverSocketContextHolder.getCompletableFutureForAccept();
		serverSocketContextHolder.setCompletableFutureForAccept(new CompletableFuture<>());
		previousCompletableFuture.complete(new CoalescingAsyncSocket(ch));
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		CompletableFuture<AsyncSocket> previousCompletableFuture = serverSocketContextHolder.getCompletableFutureForAccept();
		serverSocketContextHolder.setCompletableFutureForAccept(new CompletableFuture<>());
		previousCompletableFuture.completeExceptionally(cause);
	}

}
