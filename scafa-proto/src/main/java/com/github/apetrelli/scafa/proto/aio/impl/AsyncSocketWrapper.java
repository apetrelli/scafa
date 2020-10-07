package com.github.apetrelli.scafa.proto.aio.impl;

import java.io.InputStream;
import java.io.OutputStream;

import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.client.HostPort;

public class AsyncSocketWrapper<T extends AsyncSocket> implements AsyncSocket {

	protected T socket;
	
	public AsyncSocketWrapper(T socket) {
		this.socket = socket;
	}

	public HostPort getAddress() {
		return socket.getAddress();
	}
	
	@Override
	public void connect() {
		socket.connect();
	}
	
	@Override
	public void disconnect() {
		socket.disconnect();
	}
	
	@Override
	public void close() throws Exception {
		socket.close();
	}

	@Override
	public InputStream getInputStream() {
		return socket.getInputStream();
	}
	
	@Override
	public OutputStream getOutputStream() {
		return socket.getOutputStream();
	}

	public boolean isOpen() {
		return socket.isOpen();
	}
}
