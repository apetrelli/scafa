package com.github.apetrelli.scafa.proto.processor.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.processor.Input;
import com.github.apetrelli.scafa.proto.processor.InputProcessor;

public class PassthroughInputProcessor<P extends Input> implements InputProcessor<P> {

    private class WriteCompletionHandler implements CompletionHandler<Integer, P> {

        private CompletionHandler<P, P> completionHandler;

        private WriteCompletionHandler(CompletionHandler<P, P> readHandler) {
            this.completionHandler = readHandler;
        }

        @Override
        public void completed(Integer result, P attachment) {
        	ByteBuffer buffer = attachment.getBuffer();
        	if (result >= 0) {
				if (buffer.hasRemaining()) {
	        		channel.write(buffer, attachment, this);
	        	} else  {
	        		completionHandler.completed(attachment, attachment);
	        	}
        	} else {
        		failed(new IOException("Source channel closed"), attachment);
        	}
        }

        @Override
        public void failed(Throwable exc, P attachment) {
            completionHandler.failed(exc, attachment);
        }
    }

	private AsyncSocket channel;

	public PassthroughInputProcessor(AsyncSocket channel) {
		this.channel = channel;
	}

	@Override
	public void process(P context, CompletionHandler<P, P> completionHandler) {
		channel.write(context.getBuffer(), context, new WriteCompletionHandler(completionHandler));
	}

}
