package com.github.apetrelli.scafa.sync.proto.processor.impl;

import com.github.apetrelli.scafa.proto.data.Input;
import com.github.apetrelli.scafa.sync.proto.processor.DataHandler;
import com.github.apetrelli.scafa.sync.proto.processor.InputProcessor;
import com.github.apetrelli.scafa.sync.proto.processor.InputProcessorFactory;

public class PassthroughInputProcessorFactory implements InputProcessorFactory<DataHandler, Input> {
    @Override
    public InputProcessor<Input> create(DataHandler handler) {
        return new PassthroughInputProcessor<>(handler);
    }
}