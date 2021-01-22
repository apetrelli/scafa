package com.github.apetrelli.scafa.proto.data;

public interface ProcessingContextFactory<P extends Input> {

	P create();
}
