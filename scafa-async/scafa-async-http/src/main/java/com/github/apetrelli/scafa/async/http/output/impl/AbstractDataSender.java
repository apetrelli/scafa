package com.github.apetrelli.scafa.async.http.output.impl;

import com.github.apetrelli.scafa.async.http.output.DataSender;
import com.github.apetrelli.scafa.async.proto.socket.AsyncSocket;

public abstract class AbstractDataSender implements DataSender {

    protected AsyncSocket channel;

    public AbstractDataSender(AsyncSocket channel) {
        this.channel = channel;
    }

}