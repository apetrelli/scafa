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

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

import com.github.apetrelli.scafa.proto.processor.ByteSink;
import com.github.apetrelli.scafa.proto.processor.Input;
import com.github.apetrelli.scafa.proto.processor.InputProcessor;
import com.github.apetrelli.scafa.proto.processor.ProcessingContext;
import com.github.apetrelli.scafa.proto.processor.Status;

public abstract class AbstractInputProcessor<I extends Input, S extends ByteSink<I>, P extends ProcessingContext<I, S>> implements InputProcessor<I, S, P> {

    private S sink;

    public AbstractInputProcessor(S sink) {
        this.sink = sink;
    }

    @Override
    public void process(P context, CompletionHandler<P, P> completionHandler) {
    	I input = context.getInput();
        ByteBuffer buffer = input.getBuffer();
        if (buffer.position() < buffer.limit()) {
            Status<I, S> newStatus = context.getStatus().next(input);
            context.setStatus(newStatus);
            newStatus.out(input, sink, new CompletionHandler<Void, Void>() {

				@Override
				public void completed(Void result, Void attachment) {
                    process(context, completionHandler);
				}

				@Override
				public void failed(Throwable exc, Void attachment) {
					completionHandler.failed(exc, context);
				}
			});
        } else {
            completionHandler.completed(context, context);
        }
    }

}
