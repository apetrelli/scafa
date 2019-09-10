package com.github.apetrelli.scafa.proto.output;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import com.github.apetrelli.scafa.proto.util.AIOUtils;

public class DirectDataSender extends AbstractDataSender {

    public DirectDataSender(AsynchronousSocketChannel channel) {
        super(channel);
    }

    @Override
    public void send(ByteBuffer buffer, CompletionHandler<Void, Void> completionHandler) {
        AIOUtils.flushBuffer(buffer, channel, completionHandler);
    }

    @Override
    public void end(CompletionHandler<Void, Void> completionHandler) {
        completionHandler.completed(null, null);
    }
}
