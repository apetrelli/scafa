package com.github.apetrelli.scafa.http.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.http.HeaderHolder;
import com.github.apetrelli.scafa.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.http.output.DataSenderFactory;
import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.aio.impl.AsyncSocketWrapper;
import com.github.apetrelli.scafa.proto.output.DataSender;

public class DirectHttpAsyncSocket extends AsyncSocketWrapper implements HttpAsyncSocket {

	private static final Logger LOG = Logger.getLogger(DirectHttpAsyncSocket.class.getName());
	
	private DataSenderFactory dataSenderFactory;
	
	private DataSender dataSender;
	
	public DirectHttpAsyncSocket(AsyncSocket socket, DataSenderFactory dataSenderFactory) {
		super(socket);
		this.dataSenderFactory = dataSenderFactory;
	}

	@Override
	public void sendHeader(HeaderHolder holder, CompletionHandler<Void, Void> completionHandler) {
		dataSender = dataSenderFactory.create(holder, socket);
		ByteBuffer buffer = holder.toHeapByteBuffer();
		if (LOG.isLoggable(Level.FINEST)) {
			String request = new String(buffer.array(), 0, buffer.limit());
			LOG.finest("-- Raw request/response header");
			LOG.finest(request);
			LOG.finest("-- End of header --");
		}
		socket.flipAndFlushBuffer(buffer, completionHandler);
	}

	@Override
	public void sendData(ByteBuffer buffer, CompletionHandler<Void, Void> completionHandler) {
		if (dataSender == null) {
			completionHandler.failed(new IOException("Request never sent, data cannot be sent"), null);
		} else {
			dataSender.send(buffer, completionHandler);
		}
	}

	@Override

	public void endData(CompletionHandler<Void, Void> completionHandler) {
		if (dataSender == null) {
			completionHandler.failed(new IOException("Request never sent, data cannot be ended"), null);
		} else {
			dataSender.end(completionHandler);
			dataSender = null;
		}
	}

}
