package com.github.apetrelli.scafa.http;

import java.util.Arrays;

import com.github.apetrelli.scafa.proto.util.AsciiString;

public class HeaderName {

	private static final byte LC_Z = (byte) 122;

	private static final byte LC_A = (byte) 97;
	
	private static final byte UC_A = (byte) 65;
	
	private static final byte DIFF = LC_A - UC_A;

	private byte[] uppercased;
	
	private AsciiString original;
	
	public HeaderName(byte[] array) {
		original = new AsciiString(array);
		createUppercased();
	}
	
	public HeaderName(byte[] array, int from, int to) {
		original = new AsciiString(array, from, to);
		createUppercased();
	}
	
	public HeaderName(String string) {
		original = new AsciiString(string);
		createUppercased();
	}
	
	public HeaderName(AsciiString original) {
		this.original = original;
		createUppercased();
	}
	
	public byte[] getArray() {
		return original.getArray();
	}
	
	public int length() {
		return uppercased.length;
	}
	
	@Override
	public String toString() {
		return original.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(uppercased);
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
		return Arrays.equals(uppercased, other.uppercased);
	}

	private void createUppercased() {
		byte[] originalArray = original.getArray();
		uppercased = new byte[originalArray.length];
		for (int i = 0; i < uppercased.length; i++) {
			byte currentByte = originalArray[i];
			if (currentByte >= LC_A && currentByte <= LC_Z) {
				currentByte -= DIFF; // No need for cast this way
			}
			uppercased[i] = currentByte;
		}
	}
}
