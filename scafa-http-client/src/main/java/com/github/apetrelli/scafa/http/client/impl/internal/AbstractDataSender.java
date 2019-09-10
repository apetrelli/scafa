package com.github.apetrelli.scafa.http.client.impl.internal;

import java.nio.channels.AsynchronousSocketChannel;

public abstract class AbstractDataSender implements DataSender {

    protected AsynchronousSocketChannel channel;

    public AbstractDataSender(AsynchronousSocketChannel channel) {
        this.channel = channel;
    }

}
