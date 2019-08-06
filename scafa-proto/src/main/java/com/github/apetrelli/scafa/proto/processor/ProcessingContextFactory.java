package com.github.apetrelli.scafa.proto.processor;

public interface ProcessingContextFactory<P extends Input> {

	P create();
}
