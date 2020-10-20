package com.github.apetrelli.scafa.proto.processor.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.processor.Input;
import com.github.apetrelli.scafa.proto.processor.InputProcessor;

public class PassthroughInputProcessor<P extends Input> implements InputProcessor<P> {

	private AsyncSocket channel;

	public PassthroughInputProcessor(AsyncSocket channel) {
		this.channel = channel;
	}
	
	@Override
	public CompletableFuture<P> process(P context) {
		return channel.write(context.getBuffer()).thenCompose(x -> {
			ByteBuffer buffer = context.getBuffer();
        	if (x >= 0) {
				if (buffer.hasRemaining()) {
	        		return process(context);
	        	} else  {
	        		return CompletableFuture.completedFuture(context);
	        	}
        	} else {
        		return CompletableFuture.failedFuture(new IOException("Source channel closed"));
        	}
		});
	}
}
