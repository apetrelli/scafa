package com.github.apetrelli.scafa.proto.output;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.aio.util.AsyncUtils;

public class DirectDataSender extends AbstractDataSender {

    public DirectDataSender(AsyncSocket channel) {
        super(channel);
    }

    @Override
    public void send(ByteBuffer buffer, CompletionHandler<Void, Void> completionHandler) {
        AsyncUtils.flushBuffer(buffer, channel, completionHandler);
    }

    @Override
    public void end(CompletionHandler<Void, Void> completionHandler) {
        completionHandler.completed(null, null);
    }
}
