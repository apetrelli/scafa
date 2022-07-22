package com.github.apetrelli.scafa.http2;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class FrameHeader {
	
	public static FrameHeader fromArray(byte[] array) {
		int length = array[0] << 16 | (array[1] & 0xFF) << 8 | (array[2] & 0xFF);
		FrameType type = FrameType.fromValue(array[3]);
		byte flags = array[4];
		int streamIdentifier = array[5] << 24 | (array[6] & 0xFF) << 16 |  (array[7] & 0xFF) << 8 | (array[8] & 0xFF);
		return new FrameHeader(length, type, flags, streamIdentifier);
	}

	private final int length;
	
	private final FrameType type;
	
	private final byte flags;
	
	private final int streamIdentifier;
	
	public byte [] toByteArray() {
		byte[] frameArray = new byte[9];
		frameArray[0] = (byte) (length >> 16);
		frameArray[1] = (byte) (length >> 8);
		frameArray[2] = (byte) (length);
		frameArray[3] = type.getTypeValue();
		frameArray[4] = flags;
		frameArray[5] = (byte) (streamIdentifier >> 24);
		frameArray[6] = (byte) (streamIdentifier >> 16);
		frameArray[7] = (byte) (streamIdentifier >> 8);
		frameArray[8] = (byte) (streamIdentifier);
		
		return frameArray;
	}
}
