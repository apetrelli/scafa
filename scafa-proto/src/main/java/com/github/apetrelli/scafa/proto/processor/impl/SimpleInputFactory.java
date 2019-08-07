package com.github.apetrelli.scafa.proto.processor.impl;

import java.nio.ByteBuffer;

import com.github.apetrelli.scafa.proto.processor.Input;
import com.github.apetrelli.scafa.proto.processor.ProcessingContextFactory;
import com.github.apetrelli.scafa.proto.processor.SimpleInput;

public class SimpleInputFactory implements ProcessingContextFactory<Input> {
    @Override
    public Input create() {
        SimpleInput input = new SimpleInput();
        ByteBuffer buffer = ByteBuffer.allocate(16384);
        input.setBuffer(buffer);
        return input;
    }
}