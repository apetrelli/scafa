package com.github.apetrelli.scafa.http.sync.output.impl;

import com.github.apetrelli.scafa.http.sync.output.DataSender;
import com.github.apetrelli.scafa.proto.sync.SyncSocket;

public abstract class AbstractDataSender implements DataSender {

    protected SyncSocket channel;

    public AbstractDataSender(SyncSocket channel) {
        this.channel = channel;
    }

}
