package com.github.apetrelli.scafa.http.output.impl;

import com.github.apetrelli.scafa.http.output.DataSender;
import com.github.apetrelli.scafa.proto.aio.AsyncSocket;

public abstract class AbstractDataSender implements DataSender {

    protected AsyncSocket channel;

    public AbstractDataSender(AsyncSocket channel) {
        this.channel = channel;
    }

}
