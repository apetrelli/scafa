package com.github.apetrelli.scafa.sync.proto.jnet;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

import com.github.apetrelli.scafa.proto.IORuntimeException;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.io.InputFlow;
import com.github.apetrelli.scafa.proto.io.OutputFlow;
import com.github.apetrelli.scafa.sync.proto.SocketRuntimeException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DirectSyncSocket implements com.github.apetrelli.scafa.proto.Socket {
	
	protected final Socket channel;

	private InputFlow is;
	
	private OutputFlow os;

	@Override
	public void connect() {
		try {
			is = new SocketInputStreamInputFlow(channel.getInputStream());
			os = new OutputStreamOutputFlow(channel.getOutputStream());
		} catch (SocketException e) {
			throw new SocketRuntimeException(e);
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}

	@Override
	public HostPort getAddress() {
		HostPort retValue = null;
		retValue = new HostPort(channel.getLocalAddress().getHostName(), channel.getLocalPort());
		return retValue;
	}

	@Override
	public void disconnect() {
        if (channel != null) {
    		try {
    			channel.close();
    		} catch (IOException e) {
    			throw new IORuntimeException(e);
    		}
        }
	}

	@Override
	public boolean isOpen() {
		return !channel.isClosed();
	}

	@Override
	public InputFlow in() {
		return is;
	}

	@Override
	public OutputFlow out() {
		return os;
	}

}
