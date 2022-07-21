package com.github.apetrelli.scafa.proto.data.impl;

import com.github.apetrelli.scafa.proto.io.InputFlow;

import lombok.NonNull;

public class ProcessingContext<ST> extends SimpleInput {
	
	public ProcessingContext(InputFlow in, @NonNull ST status) {
		super(in);
		this.status = status;
	}

	@NonNull
	private ST status;

	public ST getStatus() {
		return status;
	}

	public void setStatus(ST status) {
		this.status = status;
	}
}
