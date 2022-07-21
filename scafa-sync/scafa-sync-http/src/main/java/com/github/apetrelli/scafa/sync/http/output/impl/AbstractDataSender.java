package com.github.apetrelli.scafa.sync.http.output.impl;

import com.github.apetrelli.scafa.proto.Socket;
import com.github.apetrelli.scafa.sync.http.output.DataSender;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractDataSender implements DataSender {

    protected final Socket channel;

}
