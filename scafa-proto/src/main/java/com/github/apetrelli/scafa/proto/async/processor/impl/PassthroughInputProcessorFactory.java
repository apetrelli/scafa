package com.github.apetrelli.scafa.proto.async.processor.impl;

import com.github.apetrelli.scafa.proto.async.processor.DataHandler;
import com.github.apetrelli.scafa.proto.async.processor.InputProcessor;
import com.github.apetrelli.scafa.proto.async.processor.InputProcessorFactory;
import com.github.apetrelli.scafa.proto.data.Input;

public class PassthroughInputProcessorFactory implements InputProcessorFactory<DataHandler, Input> {
    @Override
    public InputProcessor<Input> create(DataHandler handler) {
        return new PassthroughInputProcessor<>(handler);
    }
}