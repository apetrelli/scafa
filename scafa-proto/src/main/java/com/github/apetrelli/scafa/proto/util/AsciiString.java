package com.github.apetrelli.scafa.proto.util;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class AsciiString {

	private byte[] array;
	
	public AsciiString(byte[] array) {
		this.array = array;
	}
	
	public AsciiString(byte[] array, int from, int to) {
		array = Arrays.copyOfRange(array, from, to);
	}
	
	public AsciiString(String string) {
		array = string.getBytes(StandardCharsets.US_ASCII);
	}
	
	public byte[] getArray() {
		return array;
	}
	
	public int length() {
		return array.length;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(array);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AsciiString other = (AsciiString) obj;
		return Arrays.equals(array, other.array);
	}

	@Override
	public String toString() {
		return new String(array, StandardCharsets.US_ASCII);
	}
}
