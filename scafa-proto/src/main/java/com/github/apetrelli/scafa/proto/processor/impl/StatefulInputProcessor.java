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
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.proto.processor.InputProcessor;
import com.github.apetrelli.scafa.proto.processor.ProcessingContext;
import com.github.apetrelli.scafa.proto.processor.ProtocolStateMachine;

public class StatefulInputProcessor<H, P extends ProcessingContext<?>> implements InputProcessor<P> {

    private ProtocolStateMachine<H, ? super P, CompletableFuture<Void>> stateMachine;

    private H handler;

	public StatefulInputProcessor(H handler, ProtocolStateMachine<H, ? super P, CompletableFuture<Void>> stateMachine) {
        this.handler = handler;
        this.stateMachine = stateMachine;
    }
    
    @Override
    public CompletableFuture<P> process(P context) {
        ByteBuffer buffer = context.getBuffer();
        if (buffer.hasRemaining()) {
        	return stateMachine.out(context, handler).thenCompose(x -> process(context));
        } else {
            return CompletableFuture.completedFuture(context);
        }
    }

}
