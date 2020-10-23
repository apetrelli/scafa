package com.github.apetrelli.scafa.proto.processor.impl;

import com.github.apetrelli.scafa.proto.processor.DataHandler;
import com.github.apetrelli.scafa.proto.processor.Input;
import com.github.apetrelli.scafa.proto.processor.InputProcessor;
import com.github.apetrelli.scafa.proto.processor.InputProcessorFactory;

public class PassthroughInputProcessorFactory implements InputProcessorFactory<DataHandler, Input> {
    @Override
    public InputProcessor<Input> create(DataHandler handler) {
        return new PassthroughInputProcessor<>(handler);
    }
}