package com.github.apetrelli.scafa.proto.aio.sync;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.aio.CompletionHandlerFuture;
import com.github.apetrelli.scafa.proto.client.HostPort;

public class DirectSyncSocket implements AsyncSocket {
	
	protected final Socket channel;
	
	public DirectSyncSocket(Socket channel) {
		this.channel = channel;
	}

	@Override
	public CompletableFuture<Void> connect() {
		return CompletionHandlerFuture.completeEmpty();
	}

	@Override
	public HostPort getAddress() {
		HostPort retValue = null;
		retValue = new HostPort(channel.getLocalAddress().getHostName(), channel.getLocalPort());
		return retValue;
	}

	@Override
	public CompletableFuture<Void> disconnect() {
        if (channel != null) {
    		try {
    			channel.close();
    			return CompletionHandlerFuture.completeEmpty();
    		} catch (IOException e) {
    			return CompletableFuture.failedFuture(e);
    		}
        } else {
			return CompletionHandlerFuture.completeEmpty();
        }
	}

	@Override
	public CompletableFuture<Integer> read(ByteBuffer buffer) {
		try {
			InputStream is = channel.getInputStream();
			int ch;
			int count = 0;
			while (buffer.hasRemaining() && (ch = is.read()) > 0) {
				buffer.put((byte) ch);
				count++;
			}
			return CompletableFuture.completedFuture(count);
		} catch (IOException e) {
			return CompletableFuture.failedFuture(e);
		}
	}

	@Override
	public CompletableFuture<Integer> write(ByteBuffer buffer) {
		try {
			OutputStream os = channel.getOutputStream();
			int count = 0;
			while (buffer.hasRemaining()) {
				os.write(buffer.get());
				count++;
			}
			return CompletableFuture.completedFuture(count);
		} catch (IOException e) {
			return CompletableFuture.failedFuture(e);
		}
	}

	@Override
	public boolean isOpen() {
		return !channel.isClosed();
	}

}
