package com.github.apetrelli.scafa.async.proto.netty;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import com.github.apetrelli.scafa.async.proto.socket.AsyncSocket;
import com.github.apetrelli.scafa.async.proto.util.CompletionHandlerFuture;
import com.github.apetrelli.scafa.proto.client.HostPort;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.java.Log;

@Log
public class DirectAsyncSocket implements AsyncSocket {

	protected final SocketChannel channel;

	private final SocketContextHolder socketContextHolder;
	
	public DirectAsyncSocket(SocketChannel channel) {
		this.channel = channel;
		socketContextHolder = new SocketContextHolder();
		channel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
			@Override
			public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
				ByteBuf buf = (ByteBuf) msg;
				ByteBuffer byteBuffer = socketContextHolder.getByteBuffer();
				int length = buf.readableBytes();
				log.log(Level.FINEST, "Got bytes to read for uuid {0}", socketContextHolder.getCid());
				log.finest(() -> "Reading " + length + " bytes and putting in a buffer with position " + byteBuffer.position() + " and limit " + byteBuffer.limit());
				buf.getBytes(buf.readerIndex(), byteBuffer.array(), byteBuffer.position(), length);
				byteBuffer.position(byteBuffer.position() + length);
				socketContextHolder.getCompletableFutureForRead().complete(length);
				socketContextHolder.getCompletableFutureForNextRead().complete(null);
				buf.release();
			}

			@Override
			public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
				log.log(Level.WARNING, "Exception during read", cause);
				socketContextHolder.getCompletableFutureForRead().completeExceptionally(cause);
				socketContextHolder.getCompletableFutureForNextRead().complete(null);
			}

		});
		socketContextHolder.setCompletableFutureForNextRead(CompletableFuture.completedFuture(null));
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
		CompletableFuture<Integer> completableFuture = new CompletableFuture<>();
		return socketContextHolder.getCompletableFutureForNextRead().thenCompose(x -> {
			socketContextHolder.setCompletableFutureForNextRead(new CompletableFuture<>());
			socketContextHolder.setCompletableFutureForRead(completableFuture);
			socketContextHolder.setByteBuffer(buffer);
			socketContextHolder.setCid(UUID.randomUUID());
			log.log(Level.FINEST, "Calling read with uuid {0}", socketContextHolder.getCid());
			channel.read();
			return completableFuture;
		});
	}

	@Override
	public CompletableFuture<Integer> write(ByteBuffer buffer) {
		int remaining = buffer.remaining();
		ByteBuf writeBuf = Unpooled.wrappedBuffer(buffer);
//		writeBuf.writeBytes(buffer.array(), buffer.position(), remaining);
//		buffer.position(buffer.limit());
		CompletableFuture<Integer> completableFuture = new CompletableFuture<>();
		channel.writeAndFlush(writeBuf).addListener(f -> {
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
