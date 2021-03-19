package com.github.apetrelli.scafa.sync.proto.processor.impl;

import com.github.apetrelli.scafa.proto.data.Input;
import com.github.apetrelli.scafa.sync.proto.processor.DataHandler;
import com.github.apetrelli.scafa.sync.proto.processor.InputProcessor;

public class PassthroughInputProcessor<P extends Input> implements InputProcessor<P> {

	private DataHandler handler;

	public PassthroughInputProcessor(DataHandler handler) {
		this.handler = handler;
	}
	
	@Override
	public P process(P context) {
		handler.onData(context.getBuffer());
		return context;
	}
}
