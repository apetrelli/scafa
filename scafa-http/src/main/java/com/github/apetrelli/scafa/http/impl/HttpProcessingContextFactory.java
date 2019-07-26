package com.github.apetrelli.scafa.http.impl;

import com.github.apetrelli.scafa.http.HttpByteSink;
import com.github.apetrelli.scafa.http.HttpInput;
import com.github.apetrelli.scafa.http.HttpProcessingContext;
import com.github.apetrelli.scafa.http.HttpStatus;
import com.github.apetrelli.scafa.proto.processor.ProcessingContextFactory;
import com.github.apetrelli.scafa.proto.processor.Status;

public class HttpProcessingContextFactory implements ProcessingContextFactory<HttpInput, HttpByteSink, HttpProcessingContext> {

	@Override
	public HttpProcessingContext create(HttpInput input, Status<HttpInput, HttpByteSink> status) {
		return new HttpProcessingContext((HttpStatus) status, input);
	}

}
