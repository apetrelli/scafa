package com.github.apetrelli.scafa.http.proxy.ntlm;

import com.github.apetrelli.scafa.http.HttpStatus;
import com.github.apetrelli.scafa.proto.data.ProcessingContextFactory;
import com.github.apetrelli.scafa.proto.io.InputFlow;

public class NtlmHttpProcessingContextFactory implements ProcessingContextFactory<NtlmHttpProcessingContext> {

	@Override
	public NtlmHttpProcessingContext create(InputFlow in) {
		return new NtlmHttpProcessingContext(in, HttpStatus.IDLE);
	}

}
