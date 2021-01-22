package com.github.apetrelli.scafa.proto.data.impl;

public class ProcessingContext<ST> extends SimpleInput {

	private ST status;

	public ProcessingContext(ST status) {
		this.status = status;
	}

	public ST getStatus() {
		return status;
	}

	public void setStatus(ST status) {
		this.status = status;
	}
}
