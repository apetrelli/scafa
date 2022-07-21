package com.github.apetrelli.scafa.http.proxy.ntlm;

import com.github.apetrelli.scafa.http.HttpProcessingContext;
import com.github.apetrelli.scafa.http.HttpStatus;
import com.github.apetrelli.scafa.proto.io.InputFlow;

import lombok.NonNull;

public class NtlmHttpProcessingContext extends HttpProcessingContext {

	public NtlmHttpProcessingContext(InputFlow in, @NonNull HttpStatus status) {
		super(in, status);
	}

	private int bytesRead = 0;
	
	public int getBytesRead() {
		return bytesRead;
	}
	
	public void addBytesRead(int newBytes) {
		bytesRead += newBytes;
	}
}
