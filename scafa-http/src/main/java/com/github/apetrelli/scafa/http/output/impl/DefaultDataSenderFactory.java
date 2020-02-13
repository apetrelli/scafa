package com.github.apetrelli.scafa.http.output.impl;

import com.github.apetrelli.scafa.http.HeaderHolder;
import com.github.apetrelli.scafa.http.output.DataSender;
import com.github.apetrelli.scafa.http.output.DataSenderFactory;
import com.github.apetrelli.scafa.proto.aio.AsyncSocket;

public class DefaultDataSenderFactory implements DataSenderFactory {

    @Override
    public DataSender create(HeaderHolder holder, AsyncSocket channel) {
        DataSender sender;
        if ("chunked".equals(holder.getHeader("Transfer-Encoding"))) {
            sender = new ChunkedDataSender(channel);
        } else {
            sender = new DirectDataSender(channel);
        }
        return sender;
    }

}
