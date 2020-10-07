package com.github.apetrelli.scafa.http.output.impl;

import java.io.IOException;

import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.aio.impl.IORuntimeException;

public class DirectDataSender extends AbstractDataSender {

    public DirectDataSender(AsyncSocket channel) {
        super(channel);
    }

    @Override
    public void send(byte[] b, int off, int len) {
        try {
			channel.getOutputStream().write(b, off, len);
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
    }

    @Override
    public void end() {
        // Does nothing
    }
}
