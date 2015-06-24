package com.github.apetrelli.scafa.processor.impl;

import com.github.apetrelli.scafa.http.HttpInput;
import com.github.apetrelli.scafa.processor.BufferProcessor;
import com.github.apetrelli.scafa.processor.BufferProcessorFactory;
import com.github.apetrelli.scafa.processor.ByteSink;

public class ProxyBufferProcessorFactory<S extends ByteSink<HttpInput>> implements
        BufferProcessorFactory<HttpInput, S> {

    @Override
    public BufferProcessor<HttpInput, S> create(S sink) {
        return new ProxyBufferProcessor<>(sink);
    }
}
