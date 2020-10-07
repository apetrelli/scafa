package com.github.apetrelli.scafa.proto.processor.impl;

import java.io.IOException;

import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.aio.impl.IORuntimeException;
import com.github.apetrelli.scafa.proto.processor.Input;
import com.github.apetrelli.scafa.proto.processor.InputProcessor;

public class PassthroughInputProcessor<P extends Input> implements InputProcessor<P> {

	private AsyncSocket channel;

	public PassthroughInputProcessor(AsyncSocket channel) {
		this.channel = channel;
	}

	@Override
	public void process(P context) {
    	int c;
        try {
			while ((c = context.getStream().read()) >= 0) {
				channel.getOutputStream().write(c);
			}
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}

}
