package com.github.apetrelli.scafa.proto.data.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProcessingContext<ST> extends SimpleInput {

	@NonNull
	private ST status;

	public ST getStatus() {
		return status;
	}

	public void setStatus(ST status) {
		this.status = status;
	}
}
