package com.github.apetrelli.scafa.proto.aio.sync;

import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.processor.Processor;
import com.github.apetrelli.scafa.proto.processor.ProcessorFactory;

public class ThreadedProcessorFactory<H> implements ProcessorFactory<H, AsyncSocket>{

	private final ProcessorFactory<H, AsyncSocket> delegate;
	
	public ThreadedProcessorFactory(ProcessorFactory<H, AsyncSocket> delegate) {
		this.delegate = delegate;
	}
	
	@Override
	public Processor<H> create(AsyncSocket socket) {
		return new ThreadedProcessor<>(delegate.create(socket));
	}

}
