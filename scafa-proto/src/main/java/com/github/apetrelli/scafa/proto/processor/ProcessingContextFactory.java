package com.github.apetrelli.scafa.proto.processor;

public interface ProcessingContextFactory<ST, P extends ProcessingContext<ST>> {

	P create();
}
