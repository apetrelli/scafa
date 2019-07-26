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

	public I getInput() {
		return input;
	}

	public Status<I, S> next() {
		status = status.next(input);
		return status;
	}
}
