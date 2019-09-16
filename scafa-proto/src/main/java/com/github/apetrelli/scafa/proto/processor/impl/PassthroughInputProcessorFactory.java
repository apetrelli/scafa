package com.github.apetrelli.scafa.proto.processor.impl;

import java.nio.channels.AsynchronousSocketChannel;

import com.github.apetrelli.scafa.proto.processor.Handler;
import com.github.apetrelli.scafa.proto.processor.Input;
import com.github.apetrelli.scafa.proto.processor.InputProcessor;
import com.github.apetrelli.scafa.proto.processor.InputProcessorFactory;

public class PassthroughInputProcessorFactory implements InputProcessorFactory<Handler, Input> {
    private final AsynchronousSocketChannel channel;

    public PassthroughInputProcessorFactory(AsynchronousSocketChannel sourceChannel) {
        this.channel = sourceChannel;
    }

    @Override
    public InputProcessor<Input> create(Handler handler) {
        return new PassthroughInputProcessor<Input>(channel);
    }
}