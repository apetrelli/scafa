package com.github.apetrelli.scafa.http.output;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import com.github.apetrelli.scafa.http.util.HttpUtils;
import com.github.apetrelli.scafa.proto.output.AbstractDataSender;

public class ChunkedDataSender extends AbstractDataSender {

	public ChunkedDataSender(AsynchronousSocketChannel channel) {
        super(channel);
    }

    @Override
	public void send(ByteBuffer buffer, CompletionHandler<Void, Void> completionHandler) {
	    HttpUtils.sendAsChunk(buffer, channel, completionHandler);
    }

	@Override
	public void end(CompletionHandler<Void, Void> completionHandler) {
	    HttpUtils.sendEndOfChunkedTransfer(channel, completionHandler);
	}
}
