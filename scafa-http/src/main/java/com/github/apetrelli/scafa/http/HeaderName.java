package com.github.apetrelli.scafa.http;

import com.github.apetrelli.scafa.proto.util.AsciiString;

public class HeaderName extends AsciiString {

	private static final byte LC_Z = (byte) 122;

	private static final byte LC_A = (byte) 97;
	
	private static final byte UC_A = (byte) 65;
	
	private static final byte DIFF = LC_A - UC_A;
	
	public HeaderName(byte[] array) {
		super(array);
	}
	
	public HeaderName(byte[] array, int from, int to) {
		super(array, from, to);
	}
	
	public HeaderName(String string) {
		super(string);
	}
	
	public HeaderName(AsciiString original) {
		super(original.getArray(), original.getFrom(), original.getTo());
	}

	@Override
	public int hashCode() {
		int result = 1;
		for (int i = from; i < to; i++) {
			result = 31 * result + toUpper(array[i]);
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
		HeaderName other = (HeaderName) obj;
		if (length() != other.length()) {
			return false;
		}
		boolean same = true;
		for (int i = from, j = other.from; same && i < to; i++, j++) {
			same = toUpper(array[i]) == toUpper(other.array[j]);
		}
		return same;
	}

	private byte toUpper(byte element) {
		if (element >= LC_A && element <= LC_Z) {
			element -= DIFF;
		}
		return element;
	}
}
