package com.github.apetrelli.scafa.async.proto.processor.impl;

import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.async.proto.processor.DataHandler;
import com.github.apetrelli.scafa.async.proto.processor.InputProcessor;
import com.github.apetrelli.scafa.proto.data.Input;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PassthroughInputProcessor<P extends Input> implements InputProcessor<P> {

	private final DataHandler handler;
	
	@Override
	public CompletableFuture<P> process(P context) {
		return handler.onData(context.getBuffer()).thenApply(x -> context);
	}
}
