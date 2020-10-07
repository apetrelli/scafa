package com.github.apetrelli.scafa.proto.aio;

import java.io.InputStream;
import java.io.OutputStream;

import com.github.apetrelli.scafa.proto.client.HostPort;

public interface AsyncSocket extends AutoCloseable {

	HostPort getAddress();
	
	void connect();
	
	void disconnect();
	
	InputStream getInputStream();
	
	OutputStream getOutputStream();

	boolean isOpen();
}
