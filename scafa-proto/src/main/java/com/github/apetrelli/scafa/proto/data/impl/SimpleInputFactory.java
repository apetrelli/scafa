package com.github.apetrelli.scafa.proto.data.impl;

import java.nio.ByteBuffer;

import com.github.apetrelli.scafa.proto.data.Input;
import com.github.apetrelli.scafa.proto.data.ProcessingContextFactory;

public class SimpleInputFactory implements ProcessingContextFactory<Input> {
    @Override
    public Input create() {
        SimpleInput input = new SimpleInput();
        ByteBuffer buffer = ByteBuffer.allocate(16384);
        input.setBuffer(buffer);
        return input;
    }
}