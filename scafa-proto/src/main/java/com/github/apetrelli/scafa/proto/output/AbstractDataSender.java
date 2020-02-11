package com.github.apetrelli.scafa.proto.output;

import com.github.apetrelli.scafa.proto.aio.AsyncSocket;

public abstract class AbstractDataSender implements DataSender {

    protected AsyncSocket channel;

    public AbstractDataSender(AsyncSocket channel) {
        this.channel = channel;
    }

}
