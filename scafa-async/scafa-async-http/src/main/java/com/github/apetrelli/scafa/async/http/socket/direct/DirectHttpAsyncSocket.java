package com.github.apetrelli.scafa.async.http.socket.direct;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.async.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.async.http.output.DataSender;
import com.github.apetrelli.scafa.async.http.output.DataSenderFactory;
import com.github.apetrelli.scafa.async.http.output.impl.DirectDataSender;
import com.github.apetrelli.scafa.async.proto.socket.AsyncSocket;
import com.github.apetrelli.scafa.async.proto.socket.AsyncSocketWrapper;
import com.github.apetrelli.scafa.http.HeaderHolder;

public class DirectHttpAsyncSocket<H extends HeaderHolder> extends AsyncSocketWrapper<AsyncSocket> implements HttpAsyncSocket<H> {

	private static final Logger LOG = Logger.getLogger(DirectHttpAsyncSocket.class.getName());
	
	private final DataSenderFactory dataSenderFactory;
	
	private DataSender dataSender;
	
	public DirectHttpAsyncSocket(AsyncSocket socket, DataSenderFactory dataSenderFactory) {
		super(socket);
		dataSender = new DirectDataSender(socket);
		this.dataSenderFactory = dataSenderFactory;
	}
	
	@Override
	public CompletableFuture<Void> sendHeader(H holder, ByteBuffer buffer) {
		holder.fill(buffer);
		dataSender = dataSenderFactory.create(holder, socket);
		if (LOG.isLoggable(Level.FINEST)) {
			String request = new String(buffer.array(), 0, buffer.limit());
			LOG.finest("-- Raw request/response header");
			LOG.finest(request);
			LOG.finest("-- End of header --");
		}
		return socket.flipAndFlushBuffer(buffer);
	}

	@Override
	public CompletableFuture<Void> sendData(ByteBuffer buffer) {
		if (dataSender == null) {
			return CompletableFuture.failedFuture(new IOException("Request never sent, data cannot be sent"));
		} else {
			return dataSender.send(buffer);
		}
	}
	
	@Override
	public CompletableFuture<Void> endData() {
		if (dataSender == null) {
			return CompletableFuture.failedFuture(new IOException("Request never sent, data cannot be ended"));
		} else {
			return dataSender.end().thenRun(() -> dataSender = null);
		}
	}

}
