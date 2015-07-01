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
package com.github.apetrelli.scafa.aio.impl;

import java.nio.channels.AsynchronousSocketChannel;

import com.github.apetrelli.scafa.aio.ByteSinkByHandlerFactory;
import com.github.apetrelli.scafa.aio.ProcessorFactory;
import com.github.apetrelli.scafa.processor.BufferProcessorFactory;
import com.github.apetrelli.scafa.processor.ByteSink;
import com.github.apetrelli.scafa.processor.Input;
import com.github.apetrelli.scafa.processor.Processor;
import com.github.apetrelli.scafa.processor.impl.DefaultProcessor;
import com.github.apetrelli.scafa.server.Status;

public class DefaultProcessorFactory<I extends Input, S extends ByteSink<I>, H> implements ProcessorFactory<H> {

    private ByteSinkByHandlerFactory<I, S, H> factory;

    private BufferProcessorFactory<I, S> bufferProcessorFactory;

    private Status<I, S> initialStatus;

    public DefaultProcessorFactory(ByteSinkByHandlerFactory<I, S, H> factory,
            BufferProcessorFactory<I, S> bufferProcessorFactory, Status<I, S> initialStatus) {
        this.factory = factory;
        this.bufferProcessorFactory = bufferProcessorFactory;
        this.initialStatus = initialStatus;
    }

    @Override
    public Processor<H> create(AsynchronousSocketChannel client) {
        return new DefaultProcessor<>(client, factory, bufferProcessorFactory, initialStatus);
    }

}
