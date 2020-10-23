package com.github.apetrelli.scafa.proto.aio.sync;

import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.aio.ProcessorFactory;
import com.github.apetrelli.scafa.proto.processor.Processor;

public class ThreadedProcessorFactory<H> implements ProcessorFactory<H>{

	private final ProcessorFactory<H> delegate;
	
	public ThreadedProcessorFactory(ProcessorFactory<H> delegate) {
		this.delegate = delegate;
	}
	
	@Override
	public Processor<H> create(AsyncSocket socket) {
		return new ThreadedProcessor<>(delegate.create(socket));
	}

}
