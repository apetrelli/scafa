package com.github.apetrelli.scafa.proto.sync.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.sync.IORuntimeException;
import com.github.apetrelli.scafa.proto.sync.SyncSocket;

public class DirectSyncSocket implements SyncSocket {
	
	protected final Socket channel;
	
	public DirectSyncSocket(Socket channel) {
		this.channel = channel;
	}

	@Override
	public void connect() {
		// Already connected.
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
			InputStream is = channel.getInputStream();
			int ch;
			int count = 0;
			int countdown = is.available();
			if (countdown == 0 && buffer.hasRemaining()) { // No fresh data, block
				ch = is.read();
				if (ch >= 0) {
					buffer.put((byte) ch);
					count++;
					countdown = is.available();
				} else {
					return ch;
				}
				
			}
			while (buffer.hasRemaining() && countdown > 0 && (ch = is.read()) >= 0) {
				buffer.put((byte) ch);
				countdown--;
				count++;
			}
			return count;
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}

	@Override
	public int write(ByteBuffer buffer) {
		try {
			OutputStream os = channel.getOutputStream();
			int count = 0;
			while (buffer.hasRemaining()) {
				os.write(buffer.get());
				count++;
			}
			os.flush();
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
