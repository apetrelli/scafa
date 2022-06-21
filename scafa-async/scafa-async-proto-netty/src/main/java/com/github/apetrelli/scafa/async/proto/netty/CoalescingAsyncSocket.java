package com.github.apetrelli.scafa.async.proto.netty;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import com.github.apetrelli.scafa.async.proto.socket.AsyncSocket;
import com.github.apetrelli.scafa.async.proto.util.CompletionHandlerFuture;
import com.github.apetrelli.scafa.proto.client.HostPort;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.CoalescingBufferQueue;
import io.netty.channel.socket.SocketChannel;

public class CoalescingAsyncSocket implements AsyncSocket {

	protected final SocketChannel channel;
	
	private final CoalescingBufferQueue queue;
	
	private AtomicReference<CompletableFuture<Void>> readLock;
	
	public CoalescingAsyncSocket(SocketChannel channel) {
		this.channel = channel;
		queue = new CoalescingBufferQueue(channel);
		CompletableFuture<Void> lock = new CompletableFuture<>();
		readLock = new AtomicReference<>(lock);
		channel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
			@Override
			public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
				ByteBuf buf = (ByteBuf) msg;
				queue.add(buf);
				readLock.get().complete(null);
			}
		});
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
			});
			return completableFuture;
		} else {
			return CompletionHandlerFuture.completeEmpty();
		}
	}

	@Override
	public CompletableFuture<Integer> read(ByteBuffer buffer) {
		return readLock.get().thenCompose((v) -> doRead(buffer));
	}

	@Override
	public CompletableFuture<Integer> write(ByteBuffer buffer) {
		int remaining = buffer.remaining();
		ByteBuf writeBuf = Unpooled.wrappedBuffer(buffer);
		CompletableFuture<Integer> completableFuture = new CompletableFuture<>();
		channel.writeAndFlush(writeBuf).addListener(f -> {
			if (f.isSuccess()) {
				buffer.position(buffer.limit());
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

	private CompletableFuture<Integer> doRead(ByteBuffer buffer) {
		ChannelPromise promise = channel.newPromise();
		ByteBuf buf = queue.remove(buffer.remaining(), promise);
		if (queue.isEmpty()) {
			readLock.set(new CompletableFuture<>());
		}
		int length = buf.readableBytes();
		int bytesCount = Math.min(buffer.capacity(), length);
		buffer.limit(bytesCount);
		buf.readBytes(buffer);
		buf.release();
		return CompletableFuture.completedFuture(bytesCount);
	}

}
