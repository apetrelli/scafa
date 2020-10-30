package com.github.apetrelli.scafa.http.sync.output.impl;

import com.github.apetrelli.scafa.http.HeaderHolder;
import com.github.apetrelli.scafa.http.sync.output.DataSender;
import com.github.apetrelli.scafa.http.sync.output.DataSenderFactory;
import com.github.apetrelli.scafa.proto.sync.SyncSocket;

public class DefaultDataSenderFactory implements DataSenderFactory {

    @Override
    public DataSender create(HeaderHolder holder, SyncSocket channel) {
        DataSender sender;
        if ("chunked".equals(holder.getHeader("Transfer-Encoding"))) {
            sender = new ChunkedDataSender(channel);
        } else {
            sender = new DirectDataSender(channel);
        }
        return sender;
    }

}
