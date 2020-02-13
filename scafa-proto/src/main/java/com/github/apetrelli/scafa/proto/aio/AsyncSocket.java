package com.github.apetrelli.scafa.proto.aio;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

import com.github.apetrelli.scafa.proto.client.HostPort;

public interface AsyncSocket {

	public static class BufferFlusherCompletionHandler implements CompletionHandler<Integer, ByteBuffer> {
	    private final CompletionHandler<Void, Void> completionHandler;
	
	    private AsyncSocket channel;
	
	    public BufferFlusherCompletionHandler(AsyncSocket channel, CompletionHandler<Void, Void> completionHandler) {
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

	HostPort getAddress();
	
	void connect(CompletionHandler<Void, Void> handler);
	
	void disconnect(CompletionHandler<Void, Void> handler);
	
	<A> void read(ByteBuffer buffer, A attachment, CompletionHandler<Integer, ? super A> handler);
	
	<A> void write(ByteBuffer buffer, A attachment, CompletionHandler<Integer, ? super A> handler);
	
	default void flushBuffer(ByteBuffer buffer, CompletionHandler<Void, Void> completionHandler) {
	    write(buffer, buffer, new BufferFlusherCompletionHandler(this, completionHandler));
	}
	
	default void flipAndFlushBuffer(ByteBuffer buffer, CompletionHandler<Void, Void> completionHandler) {
	    buffer.flip();
	    flushBuffer(buffer, completionHandler);
	}


	boolean isOpen();
}
