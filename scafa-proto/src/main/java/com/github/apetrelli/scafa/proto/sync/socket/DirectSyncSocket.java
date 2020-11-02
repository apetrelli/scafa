package com.github.apetrelli.scafa.proto.sync.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;

import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.sync.IORuntimeException;
import com.github.apetrelli.scafa.proto.sync.SocketRuntimeException;
import com.github.apetrelli.scafa.proto.sync.SyncSocket;

public class DirectSyncSocket implements SyncSocket {
	
	protected final Socket channel;

	private InputStream is;
	
	private OutputStream os;
	
	public DirectSyncSocket(Socket channel) {
		this.channel = channel;
	}

	@Override
	public void connect() {
		try {
			is = channel.getInputStream();
			os = channel.getOutputStream();
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
	public int read(ByteBuffer buffer) {
		try {
			int count = is.read(buffer.array(), buffer.position() + buffer.arrayOffset(), buffer.remaining());
			if (count >= 0) {
				buffer.position(buffer.position() + count);
			}
			return count;
		} catch (SocketException e) {
			throw new SocketRuntimeException(e);
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}

	@Override
	public int write(ByteBuffer buffer) {
		try {
			int count = buffer.remaining();
			os.write(buffer.array(), buffer.position() + buffer.arrayOffset(), buffer.remaining());
			buffer.position(buffer.limit());
			return count;
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}

	@Override
	public boolean isOpen() {
		return !channel.isClosed();
	}

}
