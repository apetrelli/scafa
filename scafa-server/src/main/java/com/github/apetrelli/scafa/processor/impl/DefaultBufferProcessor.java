package com.github.apetrelli.scafa.processor.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.processor.BufferProcessor;
import com.github.apetrelli.scafa.processor.ByteSink;
import com.github.apetrelli.scafa.processor.Input;
import com.github.apetrelli.scafa.server.Status;

public class DefaultBufferProcessor<I extends Input, S extends ByteSink<I>> implements BufferProcessor<I, S> {

    private static final Logger LOG = Logger.getLogger(DefaultBufferProcessor.class.getName());

    private S sink;

    public DefaultBufferProcessor(S sink) {
        this.sink = sink;
    }

    @Override
    public Status<I, S> process(I input, Status<I, S> status) {
        ByteBuffer buffer = input.getBuffer();
        while (buffer.position() < buffer.limit()) {
            status = status.next(input);
            try {
                status.out(input, sink);
            } catch (IOException e) {
                LOG.log(Level.INFO, "Generic I/O error", e);
                input.setCaughtError(true);
            } catch (RuntimeException e) {
                LOG.log(Level.INFO, "Generic runtime error", e);
                input.setCaughtError(true);
            }
        }
        return status;
    }

}
