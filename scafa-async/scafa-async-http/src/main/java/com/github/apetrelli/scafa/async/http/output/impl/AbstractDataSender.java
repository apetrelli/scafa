package com.github.apetrelli.scafa.async.http.output.impl;

import com.github.apetrelli.scafa.async.http.output.DataSender;
import com.github.apetrelli.scafa.async.proto.socket.AsyncSocket;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractDataSender implements DataSender {

    protected final AsyncSocket channel;

}
