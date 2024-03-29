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
import com.github.apetrelli.scafa.sync.proto.processor.InputProcessorFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StatefulInputProcessorFactory<H, P extends ProcessingContext<?>> implements
        InputProcessorFactory<H, P> {

	private final ProtocolStateMachine<H, P, Void> stateMachine;

	@Override
	public InputProcessor<P> create(H handler) {
        return new StatefulInputProcessor<>(handler, stateMachine);
	}
}
