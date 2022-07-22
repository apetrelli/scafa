package com.github.apetrelli.scafa.http2;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
public class Stream {

	private final int identifier;
	
	@Setter
	private StreamState state;
}
