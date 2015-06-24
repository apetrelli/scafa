package com.github.apetrelli.scafa.processor;

public interface BufferProcessorFactory<I extends Input, S extends ByteSink<I>> {

    BufferProcessor<I, S> create(S sink);
}
