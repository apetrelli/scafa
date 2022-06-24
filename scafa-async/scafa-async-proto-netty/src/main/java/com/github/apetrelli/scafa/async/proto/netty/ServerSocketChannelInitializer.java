package com.github.apetrelli.scafa.async.proto.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ServerSocketChannelInitializer extends ChannelInitializer<SocketChannel> {
	
	private final SocketQueueManager socketQueueManager;

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		socketQueueManager.add(new CoalescingAsyncSocket(ch));
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		socketQueueManager.receivedException(cause);
	}

}
