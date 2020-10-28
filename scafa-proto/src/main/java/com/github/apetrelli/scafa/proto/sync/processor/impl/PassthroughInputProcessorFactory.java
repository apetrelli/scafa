package com.github.apetrelli.scafa.proto.sync.processor.impl;

import com.github.apetrelli.scafa.proto.processor.Input;
import com.github.apetrelli.scafa.proto.sync.processor.DataHandler;
import com.github.apetrelli.scafa.proto.sync.processor.InputProcessor;
import com.github.apetrelli.scafa.proto.sync.processor.InputProcessorFactory;

public class PassthroughInputProcessorFactory implements InputProcessorFactory<DataHandler, Input> {
    @Override
    public InputProcessor<Input> create(DataHandler handler) {
        return new PassthroughInputProcessor<>(handler);
    }
}