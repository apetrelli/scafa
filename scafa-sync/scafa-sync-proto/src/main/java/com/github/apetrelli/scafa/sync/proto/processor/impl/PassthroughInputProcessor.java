package com.github.apetrelli.scafa.sync.proto.processor.impl;

import com.github.apetrelli.scafa.proto.data.Input;
import com.github.apetrelli.scafa.sync.proto.processor.DataHandler;
import com.github.apetrelli.scafa.sync.proto.processor.InputProcessor;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PassthroughInputProcessor<P extends Input> implements InputProcessor<P> {

	private final DataHandler handler;
	
	@Override
	public P process(P context) {
		handler.onData(context.in().readBuffer());
		return context;
	}
}
