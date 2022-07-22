package com.github.apetrelli.scafa.http;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class BaseHttpConversation implements HttpConversation {

	private final HeaderHolder headers;
	
	@Override
	public HeaderHolder headers() {
		return headers;
	}
}
