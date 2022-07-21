package com.github.apetrelli.scafa.proto.data.impl;

import com.github.apetrelli.scafa.proto.data.Input;
import com.github.apetrelli.scafa.proto.io.InputFlow;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SimpleInput implements Input {

	protected final InputFlow in;

	@Override
	public InputFlow in() {
		return in;
	}
}
