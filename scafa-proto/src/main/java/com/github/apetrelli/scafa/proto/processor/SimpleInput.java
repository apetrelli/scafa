package com.github.apetrelli.scafa.proto.processor;

import java.io.InputStream;

public class SimpleInput implements Input {

	private InputStream stream;

	@Override
	public InputStream getStream() {
		return stream;
	}
	
	public void setStream(InputStream stream) {
		this.stream = stream;
	}
	
}
