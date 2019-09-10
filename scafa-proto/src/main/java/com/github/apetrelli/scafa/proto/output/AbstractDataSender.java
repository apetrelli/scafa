package com.github.apetrelli.scafa.proto.output;

import java.nio.channels.AsynchronousSocketChannel;

public abstract class AbstractDataSender implements DataSender {

    protected AsynchronousSocketChannel channel;

    public AbstractDataSender(AsynchronousSocketChannel channel) {
        this.channel = channel;
    }

}
