package com.github.apetrelli.scafa.http.impl;

import com.github.apetrelli.scafa.http.HttpProcessingContext;
import com.github.apetrelli.scafa.http.HttpStatus;
import com.github.apetrelli.scafa.proto.data.ProcessingContextFactory;
import com.github.apetrelli.scafa.proto.io.InputFlow;

public class HttpProcessingContextFactory implements ProcessingContextFactory<HttpProcessingContext> {

	@Override
	public HttpProcessingContext create(InputFlow in) {
		return new HttpProcessingContext(in, HttpStatus.IDLE);
	}

}
