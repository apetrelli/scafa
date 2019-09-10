package com.github.apetrelli.scafa.http.client.impl.internal;

import java.nio.channels.AsynchronousSocketChannel;

import com.github.apetrelli.scafa.http.HeaderHolder;

public class DefaultDataSenderFactory implements DataSenderFactory {

    @Override
    public DataSender create(HeaderHolder holder, AsynchronousSocketChannel channel) {
        DataSender sender;
        if ("chunked".equals(holder.getHeader("Transfer-Encoding"))) {
            sender = new ChunkedDataSender(channel);
        } else {
            sender = new DirectDataSender(channel);
        }
        return sender;
    }

}
