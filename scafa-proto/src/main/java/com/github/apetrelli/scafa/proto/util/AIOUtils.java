package com.github.apetrelli.scafa.proto.util;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class AIOUtils {

	public static class BufferFlusherCompletionHandler implements CompletionHandler<Integer, ByteBuffer> {
	    private final CompletionHandler<Void, Void> completionHandler;
	
	    private AsynchronousSocketChannel channel;
	
	    BufferFlusherCompletionHandler(AsynchronousSocketChannel channel, CompletionHandler<Void, Void> completionHandler) {
	        this.channel = channel;
	        this.completionHandler = completionHandler;
	    }
	
	    @Override
	    public void completed(Integer result, ByteBuffer attachment) {
	        if (attachment.hasRemaining()) {
	            channel.write(attachment, attachment, this);
	        } else {
	            completionHandler.completed(null, null);
	        }
	    }
	
	    @Override
	    public void failed(Throwable exc, ByteBuffer attachment) {
	        completionHandler.failed(exc, null);
	    }
	}

	private AIOUtils() { }

	public static void flushBuffer(ByteBuffer buffer, AsynchronousSocketChannel channelToSend, CompletionHandler<Void, Void> completionHandler) {
	    channelToSend.write(buffer, buffer, new BufferFlusherCompletionHandler(channelToSend, completionHandler));
	}

	public static void flipAndFlushBuffer(ByteBuffer buffer, AsynchronousSocketChannel channelToSend, CompletionHandler<Void, Void> completionHandler) {
	    buffer.flip();
	    flushBuffer(buffer, channelToSend, completionHandler);
	}
}
