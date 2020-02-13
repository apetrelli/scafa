package com.github.apetrelli.scafa.proto.output;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

import com.github.apetrelli.scafa.proto.aio.AsyncSocket;

public class DirectDataSender extends AbstractDataSender {

    public DirectDataSender(AsyncSocket channel) {
        super(channel);
    }

    @Override
    public void send(ByteBuffer buffer, CompletionHandler<Void, Void> completionHandler) {
        channel.flushBuffer(buffer, completionHandler);
    }

    @Override
    public void end(CompletionHandler<Void, Void> completionHandler) {
        completionHandler.completed(null, null);
    }
}
