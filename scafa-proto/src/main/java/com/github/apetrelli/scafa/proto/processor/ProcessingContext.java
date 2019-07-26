package com.github.apetrelli.scafa.proto.processor;

public class ProcessingContext<I extends Input, S extends ByteSink<I>> {

	private Status<I, S> status;

	private I input;

	public ProcessingContext(Status<I, S> status, I input) {
		this.status = status;
		this.input = input;
	}

	public Status<I, S> getStatus() {
		return status;
	}

	public void setStatus(Status<I, S> status) {
		this.status = status;
	}

	public I getInput() {
		return input;
	}
}
