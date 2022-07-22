package com.github.apetrelli.scafa.http2;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum FrameType {

	DATA, HEADERS, PRIORITY, RST_STREAM, SETTINGS, PUSH_PROMISE, PING, GOAWAY, WINDOW_UPDATE, CONTINUATION;
	
	public static FrameType fromValue(byte typeValue) {
		return FrameType.values()[(int) typeValue];
	}
	
	public byte getTypeValue() {
		return (byte) ordinal();
	}
}
