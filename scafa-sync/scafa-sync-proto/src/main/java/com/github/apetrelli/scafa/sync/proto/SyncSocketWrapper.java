package com.github.apetrelli.scafa.sync.proto;

import com.github.apetrelli.scafa.proto.Socket;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.io.InputFlow;
import com.github.apetrelli.scafa.proto.io.OutputFlow;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SyncSocketWrapper<T extends Socket> implements Socket {

	protected final T socket;

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
	public InputFlow in() {
		return socket.in();
	}
	
	@Override
	public OutputFlow out() {
		return socket.out();
	}
	
	public boolean isOpen() {
		return socket.isOpen();
	}
	
	@Override
	public void close() {
		socket.close();
	}
}
