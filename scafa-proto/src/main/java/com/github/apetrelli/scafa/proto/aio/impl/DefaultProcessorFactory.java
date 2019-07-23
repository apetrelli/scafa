/**
 * Scafa - A universal non-caching proxy for the road warrior
 * Copyright (C) 2015  Antonio Petrelli
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.apetrelli.scafa.proto.aio.impl;

import java.nio.channels.AsynchronousSocketChannel;

import com.github.apetrelli.scafa.proto.aio.ByteSinkFactory;
import com.github.apetrelli.scafa.proto.aio.ProcessorFactory;
import com.github.apetrelli.scafa.proto.processor.InputProcessorFactory;
import com.github.apetrelli.scafa.proto.processor.ByteSink;
import com.github.apetrelli.scafa.proto.processor.Input;
import com.github.apetrelli.scafa.proto.processor.Processor;
import com.github.apetrelli.scafa.proto.processor.Status;
import com.github.apetrelli.scafa.proto.processor.impl.DefaultProcessor;

public class DefaultProcessorFactory<I extends Input, S extends ByteSink<I>, H> implements ProcessorFactory<H> {

    private ByteSinkFactory<I, S, H> factory;

    private InputProcessorFactory<I, S> inputProcessorFactory;

    private Status<I, S> initialStatus;

    public DefaultProcessorFactory(ByteSinkFactory<I, S, H> factory,
            InputProcessorFactory<I, S> inputProcessorFactory, Status<I, S> initialStatus) {
        this.factory = factory;
        this.inputProcessorFactory = inputProcessorFactory;
        this.initialStatus = initialStatus;
    }

    @Override
    public Processor<H> create(AsynchronousSocketChannel client) {
        return new DefaultProcessor<>(client, factory, inputProcessorFactory, initialStatus);
    }

}
