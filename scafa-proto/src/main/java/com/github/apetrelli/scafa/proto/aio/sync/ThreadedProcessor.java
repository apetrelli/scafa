package com.github.apetrelli.scafa.proto.aio.sync;

import com.github.apetrelli.scafa.proto.processor.Processor;

public class ThreadedProcessor<H> implements Processor<H> {

	private final Processor<H> delegate;

	public ThreadedProcessor(Processor<H> delegate) {
		this.delegate = delegate;
	}

	@Override
	public void process(H handler) {
		Thread.startVirtualThread(() -> delegate.process(handler));
	}

}
