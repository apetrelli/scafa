package com.github.apetrelli.scafa.processor;

import com.github.apetrelli.scafa.server.Status;

public interface BufferProcessor<I extends Input, S extends ByteSink<I>> {

    Status<I, S> process(I input, Status<I, S> status);
}
