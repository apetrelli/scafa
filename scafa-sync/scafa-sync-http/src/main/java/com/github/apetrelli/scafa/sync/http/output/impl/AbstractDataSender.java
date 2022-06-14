package com.github.apetrelli.scafa.sync.http.output.impl;

import com.github.apetrelli.scafa.sync.http.output.DataSender;
import com.github.apetrelli.scafa.sync.proto.SyncSocket;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractDataSender implements DataSender {

    protected final SyncSocket channel;

}
