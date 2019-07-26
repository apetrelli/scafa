package com.github.apetrelli.scafa.http.impl;

import com.github.apetrelli.scafa.http.HttpInput;
import com.github.apetrelli.scafa.http.HttpProcessingContext;
import com.github.apetrelli.scafa.http.HttpStatus;
import com.github.apetrelli.scafa.proto.processor.ProcessingContextFactory;

public class HttpProcessingContextFactory implements ProcessingContextFactory<HttpInput, HttpStatus, HttpProcessingContext> {

	@Override
	public HttpProcessingContext create(HttpInput input, HttpStatus status) {
		return new HttpProcessingContext(status, input);
	}

}
