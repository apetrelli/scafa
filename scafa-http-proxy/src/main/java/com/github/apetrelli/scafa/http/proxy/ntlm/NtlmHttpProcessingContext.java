package com.github.apetrelli.scafa.http.proxy.ntlm;

import com.github.apetrelli.scafa.http.HttpProcessingContext;
import com.github.apetrelli.scafa.http.HttpStatus;

public class NtlmHttpProcessingContext extends HttpProcessingContext {

	public NtlmHttpProcessingContext(HttpStatus status) {
		super(status);
	}

	private int bytesRead = 0;
	
	public int getBytesRead() {
		return bytesRead;
	}
	
	public void addBytesRead(int newBytes) {
		bytesRead += newBytes;
	}
}
