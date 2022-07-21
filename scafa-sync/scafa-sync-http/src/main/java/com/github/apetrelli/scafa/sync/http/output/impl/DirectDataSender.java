package com.github.apetrelli.scafa.sync.http.output.impl;

import com.github.apetrelli.scafa.proto.Socket;
import com.github.apetrelli.scafa.proto.io.FlowBuffer;

public class DirectDataSender extends AbstractDataSender {

    public DirectDataSender(Socket channel) {
        super(channel);
    }
    
    @Override
    public void send(FlowBuffer buffer) {
        channel.out().write(buffer);
        channel.out().flush();
    }
    
    @Override
    public void end() {
		// Do nothing.
    }
}
