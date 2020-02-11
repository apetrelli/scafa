package com.github.apetrelli.scafa.proto.processor.impl;

import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.processor.Handler;
import com.github.apetrelli.scafa.proto.processor.Input;
import com.github.apetrelli.scafa.proto.processor.InputProcessor;
import com.github.apetrelli.scafa.proto.processor.InputProcessorFactory;

public class PassthroughInputProcessorFactory implements InputProcessorFactory<Handler, Input> {
    private final AsyncSocket channel;

    public PassthroughInputProcessorFactory(AsyncSocket sourceChannel) {
        this.channel = sourceChannel;
    }

    @Override
    public InputProcessor<Input> create(Handler handler) {
        return new PassthroughInputProcessor<Input>(channel);
    }
}