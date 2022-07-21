package com.github.apetrelli.scafa.proto.data.impl;

import com.github.apetrelli.scafa.proto.data.Input;
import com.github.apetrelli.scafa.proto.data.ProcessingContextFactory;
import com.github.apetrelli.scafa.proto.io.InputFlow;

public class SimpleInputFactory implements ProcessingContextFactory<Input> {
    @Override
    public Input create(InputFlow in) {
        return new SimpleInput(in);
    }
}