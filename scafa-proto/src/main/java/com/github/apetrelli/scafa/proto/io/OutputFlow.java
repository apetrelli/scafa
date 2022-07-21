package com.github.apetrelli.scafa.proto.io;

public interface OutputFlow {
	
	OutputFlow write(byte currentByte);
	
	OutputFlow write(byte[] bytes, int from, int length);
	
	OutputFlow flush();
	
	default OutputFlow write(byte[] bytes) {
		return write(bytes, 0, bytes.length);
	}
	
	default OutputFlow write(FlowBuffer buffer) {
		return write(buffer.array(), 0, buffer.length());
	}

}
