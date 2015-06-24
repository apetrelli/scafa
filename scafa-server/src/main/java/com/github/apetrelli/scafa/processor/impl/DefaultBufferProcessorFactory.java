package com.github.apetrelli.scafa.processor.impl;

import com.github.apetrelli.scafa.processor.BufferProcessor;
import com.github.apetrelli.scafa.processor.BufferProcessorFactory;
import com.github.apetrelli.scafa.processor.ByteSink;
import com.github.apetrelli.scafa.processor.Input;

public class DefaultBufferProcessorFactory<I extends Input, S extends ByteSink<I>> implements
        BufferProcessorFactory<I, S> {

    @Override
    public BufferProcessor<I, S> create(S sink) {
        return new DefaultBufferProcessor<>(sink);
    }
}
