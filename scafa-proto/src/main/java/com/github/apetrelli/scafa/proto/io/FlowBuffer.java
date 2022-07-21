package com.github.apetrelli.scafa.proto.io;

public class FlowBuffer {
	
	public static FlowBuffer wrap(byte[] data) {
		return new FlowBuffer(data);
	}

	private final byte[] array;
	
	private int length = 0;
	
	public FlowBuffer(int bufferSize) {
		array = new byte[bufferSize];
	}
	
	private FlowBuffer(byte[] array) {
		this.array = array;
		length = array.length;
	}
	
	public byte[] array() {
		return array;
	}
	
	public int length() {
		return length;
	}
	
	public void length(int length) {
		this.length = length;
	}
	
	public int maxLength() {
		return array.length;
	}
}
