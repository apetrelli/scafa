package com.github.apetrelli.scafa.http.output.impl;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

import com.github.apetrelli.scafa.http.output.DataSender;

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
