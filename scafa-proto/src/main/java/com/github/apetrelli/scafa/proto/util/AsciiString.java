package com.github.apetrelli.scafa.proto.util;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class AsciiString {

	protected byte[] array;
	
	protected int from;
	
	protected int to;
	
	public AsciiString(byte[] array) {
		this(array, 0, array.length);
	}
	
	public AsciiString(byte[] array, int from, int to) {
		this.array = array;
		this.from = from;
		this.to = to;
	}
	
	public AsciiString(String string) {
		this(string.getBytes(StandardCharsets.US_ASCII));
	}
	
	public byte[] getArray() {
		return array;
	}
	
	public int getFrom() {
		return from;
	}
	
	public int getTo() {
		return to;
	}
	
	public int length() {
		return to - from;
	}
	
	public boolean startsWith(AsciiString toCompare) {
		if (toCompare.length() <= length()) {
			return Arrays.equals(array, from, from + (toCompare.to - toCompare.from), toCompare.array, toCompare.from,
					toCompare.to);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		int result = 1;
		for (int i = from; i < to; i++) {
			result = 31 * result + array[i];
		}
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
		return Arrays.equals(array, from, to, other.array, other.from, other.to);
	}

	@Override
	public String toString() {
		return new String(array, from, to - from, StandardCharsets.US_ASCII);
	}
}
