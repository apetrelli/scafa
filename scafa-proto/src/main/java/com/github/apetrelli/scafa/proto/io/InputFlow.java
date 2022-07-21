package com.github.apetrelli.scafa.proto.io;

public interface InputFlow {
	
	byte read();

	FlowBuffer read(long maxLength);
	
	FlowBuffer readBuffer();
	
	boolean hasData();
}
