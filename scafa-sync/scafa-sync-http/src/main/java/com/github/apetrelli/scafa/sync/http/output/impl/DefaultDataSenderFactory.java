package com.github.apetrelli.scafa.sync.http.output.impl;

import static com.github.apetrelli.scafa.http.HttpHeaders.CHUNKED;
import static com.github.apetrelli.scafa.http.HttpHeaders.TRANSFER_ENCODING;

import com.github.apetrelli.scafa.http.HeaderHolder;
import com.github.apetrelli.scafa.sync.http.output.DataSender;
import com.github.apetrelli.scafa.sync.http.output.DataSenderFactory;
import com.github.apetrelli.scafa.sync.proto.SyncSocket;

public class DefaultDataSenderFactory implements DataSenderFactory {

    @Override
    public DataSender create(HeaderHolder holder, SyncSocket channel) {
        DataSender sender;
        if (CHUNKED.equals(holder.getHeader(TRANSFER_ENCODING))) {
            sender = new ChunkedDataSender(channel);
        } else {
            sender = new DirectDataSender(channel);
        }
        return sender;
    }

}
