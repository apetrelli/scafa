package com.github.apetrelli.scafa.async.proto.netty;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.async.proto.socket.AsyncSocket;
import com.github.apetrelli.scafa.async.proto.util.CompletionHandlerFuture;
import com.github.apetrelli.scafa.proto.client.HostPort;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;

public class DirectAsyncSocket implements AsyncSocket {

	protected final SocketChannel channel;

	private final SocketContextHolder socketContextHolder;

	private final ByteBuf writeBuf;
	
	public DirectAsyncSocket(SocketChannel channel) {
		this.channel = channel;
		socketContextHolder = new SocketContextHolder();
		channel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
			@Override
			public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
				ByteBuf buf = (ByteBuf) msg;
				socketContextHolder.getByteBuffer().put(buf.array(), buf.readerIndex(), buf.writerIndex());
				socketContextHolder.getCompletableFutureForRead().complete(buf.readableBytes());
				socketContextHolder.getCompletableFutureForNextRead().complete(null);
				buf.release();
			}

			@Override
			public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
				socketContextHolder.getCompletableFutureForRead().completeExceptionally(cause);
				socketContextHolder.getCompletableFutureForNextRead().complete(null);
			}

		});
		socketContextHolder.setCompletableFutureForNextRead(CompletableFuture.completedFuture(null));
		writeBuf = channel.alloc().buffer(16384, 16384);
	}

	@Override
	public CompletableFuture<Void> connect() {
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public HostPort getAddress() {
		InetSocketAddress address = channel.localAddress();
		return new HostPort(address.getHostName(), address.getPort());
	}

	@Override
	public CompletableFuture<Void> disconnect() {
		if (channel != null && channel.isOpen()) {
			final CompletableFuture<Void> completableFuture = new CompletableFuture<>();
			channel.close().addListener(f -> {
				if (f.isSuccess()) {
					completableFuture.complete(null);
				} else {
					completableFuture.completeExceptionally(f.cause());
				}
				writeBuf.release();
			});
			return completableFuture;
		} else {
			return CompletionHandlerFuture.completeEmpty();
		}
	}

	@Override
	public CompletableFuture<Integer> read(ByteBuffer buffer) {
		CompletableFuture<Integer> completableFuture = new CompletableFuture<>();
		return socketContextHolder.getCompletableFutureForNextRead().thenCompose(x -> {
			socketContextHolder.setCompletableFutureForNextRead(new CompletableFuture<>());
			socketContextHolder.setCompletableFutureForRead(completableFuture);
			socketContextHolder.setByteBuffer(buffer);
			channel.read();
			return completableFuture;
		});
	}

	@Override
	public CompletableFuture<Integer> write(ByteBuffer buffer) {
		int remaining = buffer.remaining();
		writeBuf.writeBytes(buffer.array(), buffer.position(), buffer.limit());
		CompletableFuture<Integer> completableFuture = new CompletableFuture<>();
		channel.write(writeBuf).addListener(f -> {
			if (f.isSuccess()) {
				completableFuture.complete(remaining);
			} else {
				completableFuture.completeExceptionally(f.cause());
			}
		});
		return completableFuture;
	}

	@Override
	public boolean isOpen() {
		return channel.isOpen();
	}

}
