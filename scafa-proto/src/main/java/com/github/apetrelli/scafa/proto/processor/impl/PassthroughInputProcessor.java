package com.github.apetrelli.scafa.proto.processor.impl;

import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.proto.processor.DataHandler;
import com.github.apetrelli.scafa.proto.processor.Input;
import com.github.apetrelli.scafa.proto.processor.InputProcessor;

public class PassthroughInputProcessor<P extends Input> implements InputProcessor<P> {

	private DataHandler handler;

	public PassthroughInputProcessor(DataHandler handler) {
		this.handler = handler;
	}
	
	@Override
	public CompletableFuture<P> process(P context) {
		return handler.onData(context.getBuffer()).thenApply(x -> context);
	}
}
