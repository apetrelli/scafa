package com.github.apetrelli.scafa.http.sync.output.impl;

import java.nio.ByteBuffer;

import com.github.apetrelli.scafa.sync.proto.SyncSocket;

public class DirectDataSender extends AbstractDataSender {

    public DirectDataSender(SyncSocket channel) {
        super(channel);
    }
    
    @Override
    public void send(ByteBuffer buffer) {
        channel.flushBuffer(buffer);
    }
    
    @Override
    public void end() {
		// Do nothing.
    }
}
