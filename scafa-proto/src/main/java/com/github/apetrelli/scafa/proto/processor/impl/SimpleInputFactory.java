package com.github.apetrelli.scafa.proto.processor.impl;

import java.io.InputStream;

import com.github.apetrelli.scafa.proto.processor.Input;
import com.github.apetrelli.scafa.proto.processor.ProcessingContextFactory;
import com.github.apetrelli.scafa.proto.processor.SimpleInput;

public class SimpleInputFactory implements ProcessingContextFactory<Input> {
    @Override
    public Input create(InputStream stream) {
        SimpleInput input = new SimpleInput();
        input.setStream(stream);
        return input;
    }
}