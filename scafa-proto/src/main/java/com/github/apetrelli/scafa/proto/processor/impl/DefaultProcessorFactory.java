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
package com.github.apetrelli.scafa.proto.processor.impl;

import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.processor.Handler;
import com.github.apetrelli.scafa.proto.processor.Input;
import com.github.apetrelli.scafa.proto.processor.InputProcessorFactory;
import com.github.apetrelli.scafa.proto.processor.ProcessingContextFactory;
import com.github.apetrelli.scafa.proto.processor.Processor;
import com.github.apetrelli.scafa.proto.processor.ProcessorFactory;

public class DefaultProcessorFactory<P extends Input, H extends Handler>
		implements ProcessorFactory<H, AsyncSocket> {

	private InputProcessorFactory<H, P> inputProcessorFactory;

	private ProcessingContextFactory<P> processingContextFactory;

	public DefaultProcessorFactory(InputProcessorFactory<H, P> inputProcessorFactory,
			ProcessingContextFactory<P> processingContextFactory) {
		this.inputProcessorFactory = inputProcessorFactory;
		this.processingContextFactory = processingContextFactory;
	}

	@Override
	public Processor<H> create(AsyncSocket client) {
		return new DefaultProcessor<>(client, inputProcessorFactory, processingContextFactory);
	}

}
