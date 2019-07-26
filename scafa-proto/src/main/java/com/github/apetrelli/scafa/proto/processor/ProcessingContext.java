package com.github.apetrelli.scafa.proto.processor;

public class ProcessingContext<I extends Input, ST> {

	private ST status;

	private I input;

	public ProcessingContext(ST status, I input) {
		this.status = status;
		this.input = input;
	}

	public ST getStatus() {
		return status;
	}

	public void setStatus(ST status) {
		this.status = status;
	}

	public I getInput() {
		return input;
	}
}
