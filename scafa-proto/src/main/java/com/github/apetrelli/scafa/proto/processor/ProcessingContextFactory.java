package com.github.apetrelli.scafa.proto.processor;

public interface ProcessingContextFactory<I extends Input, S extends ByteSink<I>, P extends ProcessingContext<I, S>> {

	P create(I input, Status<I, S> status);
}
