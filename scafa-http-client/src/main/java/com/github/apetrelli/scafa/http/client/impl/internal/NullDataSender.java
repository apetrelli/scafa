package com.github.apetrelli.scafa.http.client.impl.internal;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

public class NullDataSender implements DataSender {

    @Override
    public void send(ByteBuffer buffer, CompletionHandler<Void, Void> completionHandler) {
        completionHandler.completed(null, null);
    }

    @Override
    public void end(CompletionHandler<Void, Void> completionHandler) {
        completionHandler.completed(null, null);
    }

}
