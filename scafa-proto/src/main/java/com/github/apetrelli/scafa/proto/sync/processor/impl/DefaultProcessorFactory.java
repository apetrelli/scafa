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
package com.github.apetrelli.scafa.proto.sync.processor.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.github.apetrelli.scafa.proto.processor.Handler;
import com.github.apetrelli.scafa.proto.processor.Input;
import com.github.apetrelli.scafa.proto.processor.ProcessingContextFactory;
import com.github.apetrelli.scafa.proto.processor.Processor;
import com.github.apetrelli.scafa.proto.processor.ProcessorFactory;
import com.github.apetrelli.scafa.proto.sync.SyncSocket;
import com.github.apetrelli.scafa.proto.sync.processor.InputProcessorFactory;

public class DefaultProcessorFactory<P extends Input, H extends Handler>
		implements ProcessorFactory<H, SyncSocket>, AutoCloseable {

	private InputProcessorFactory<H, P> inputProcessorFactory;

	private ProcessingContextFactory<P> processingContextFactory;
	
	private AtomicLong currentId = new AtomicLong(0L);
	
	private Map<Long, DefaultProcessor<P, H>> id2processor;

	public DefaultProcessorFactory(InputProcessorFactory<H, P> inputProcessorFactory,
			ProcessingContextFactory<P> processingContextFactory) {
		this.inputProcessorFactory = inputProcessorFactory;
		this.processingContextFactory = processingContextFactory;
		id2processor = new ConcurrentHashMap<>();
	}

	@Override
	public Processor<H> create(SyncSocket client) {
		long id = currentId.getAndIncrement();
		DefaultProcessor<P, H> processor = new DefaultProcessor<>(id, client, inputProcessorFactory, processingContextFactory, this);
		id2processor.put(id, processor);
		return processor;
	}

	public void release(long id) {
		id2processor.remove(id);
	}

	@Override
	public void close() {
		id2processor.values().forEach(x -> {
			try {
				x.disconnectFinally();
			} catch (RuntimeException e) {
				// Ignore+
			}
		});
		id2processor.clear();
	}
}
