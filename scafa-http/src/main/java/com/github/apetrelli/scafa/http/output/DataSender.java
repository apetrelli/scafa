package com.github.apetrelli.scafa.http.output;

public interface DataSender {

	void send(byte[] b, int off, int len);

	void end();
}
