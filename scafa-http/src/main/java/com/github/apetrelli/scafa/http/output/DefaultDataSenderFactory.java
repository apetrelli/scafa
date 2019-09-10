package com.github.apetrelli.scafa.http.output;

import java.nio.channels.AsynchronousSocketChannel;

import com.github.apetrelli.scafa.http.HeaderHolder;
import com.github.apetrelli.scafa.proto.output.DataSender;
import com.github.apetrelli.scafa.proto.output.DirectDataSender;

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
