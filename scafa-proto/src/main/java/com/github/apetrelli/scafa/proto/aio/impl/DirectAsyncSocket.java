package com.github.apetrelli.scafa.proto.aio.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.client.HostPort;

public class DirectAsyncSocket implements AsyncSocket {
	
	protected Socket channel;
	
	public DirectAsyncSocket(Socket channel) {
		this.channel = channel;
	}

	@Override
	public void connect() {
		// Already connected
	}
	
	@Override
	public HostPort getAddress() {
		return HostPort.fromSocketAddress(channel.getLocalSocketAddress());
	}
	
	@Override
	public void disconnect() {
        if (channel != null && !channel.isClosed()) {
    		try {
    			channel.close();
    		} catch (IOException e) {
    			throw new IORuntimeException(e);
    		}
        }
	}
	
	@Override
	public void close() {
		disconnect();
	}
	
	@Override
	public boolean isOpen() {
		return !channel.isClosed();
	}
	
	@Override
	public InputStream getInputStream() {
		try {
			return channel.getInputStream();
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}
	
	@Override
	public OutputStream getOutputStream() {
		try {
			return channel.getOutputStream();
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}
}
