package com.github.apetrelli.scafa.proto.processor;

public interface ProcessingContextFactory<I extends Input, ST, P extends ProcessingContext<I, ST>> {

	P create(I input, ST status);
}
