package com.github.apetrelli.scafa.http.output.impl;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.proto.async.AsyncSocket;
import com.github.apetrelli.scafa.proto.async.util.CompletionHandlerFuture;

public class DirectDataSender extends AbstractDataSender {

    public DirectDataSender(AsyncSocket channel) {
        super(channel);
    }
    
    @Override
    public CompletableFuture<Void> send(ByteBuffer buffer) {
        return channel.flushBuffer(buffer);
    }
    
    @Override
    public CompletableFuture<Void> end() {
		return CompletionHandlerFuture.completeEmpty();
    }
}
