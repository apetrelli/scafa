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
package com.github.apetrelli.scafa.sync.proto.processor.impl;

import com.github.apetrelli.scafa.proto.data.impl.ProcessingContext;
import com.github.apetrelli.scafa.proto.processor.ProtocolStateMachine;
import com.github.apetrelli.scafa.sync.proto.processor.InputProcessor;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StatefulInputProcessor<H, P extends ProcessingContext<?>> implements InputProcessor<P> {

    private final H handler;

    private final ProtocolStateMachine<H, ? super P> stateMachine;
    
    @Override
    public P process(P context) {
    	stateMachine.out(context, handler);
        return context;
    }

}
