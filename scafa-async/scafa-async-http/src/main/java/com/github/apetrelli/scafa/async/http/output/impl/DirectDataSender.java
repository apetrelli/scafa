package com.github.apetrelli.scafa.async.http.output.impl;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.async.proto.socket.AsyncSocket;
import com.github.apetrelli.scafa.async.proto.util.CompletionHandlerFuture;

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
