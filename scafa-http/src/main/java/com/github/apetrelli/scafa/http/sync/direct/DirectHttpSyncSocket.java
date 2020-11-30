package com.github.apetrelli.scafa.http.sync.direct;

import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.http.HeaderHolder;
import com.github.apetrelli.scafa.http.HttpException;
import com.github.apetrelli.scafa.http.sync.HttpSyncSocket;
import com.github.apetrelli.scafa.http.sync.output.DataSender;
import com.github.apetrelli.scafa.http.sync.output.DataSenderFactory;
import com.github.apetrelli.scafa.http.sync.output.impl.DirectDataSender;
import com.github.apetrelli.scafa.proto.sync.SyncSocket;
import com.github.apetrelli.scafa.proto.sync.socket.SyncSocketWrapper;

public class DirectHttpSyncSocket<H extends HeaderHolder> extends SyncSocketWrapper<SyncSocket> implements HttpSyncSocket<H> {

	private static final Logger LOG = Logger.getLogger(DirectHttpSyncSocket.class.getName());
	
	private DataSenderFactory dataSenderFactory;
	
	private DataSender dataSender;
	
	public DirectHttpSyncSocket(SyncSocket socket, DataSenderFactory dataSenderFactory) {
		super(socket);
		dataSender = new DirectDataSender(socket);
		this.dataSenderFactory = dataSenderFactory;
	}
	
	@Override
	public void sendHeader(H holder) {
		dataSender = dataSenderFactory.create(holder, socket);
		ByteBuffer buffer = holder.toHeapByteBuffer();
		if (LOG.isLoggable(Level.FINEST)) {
			String request = new String(buffer.array(), 0, buffer.limit());
			LOG.finest("-- Raw request/response header");
			LOG.finest(request);
			LOG.finest("-- End of header --");
		}
		socket.flipAndFlushBuffer(buffer);
	}

	@Override
	public void sendData(ByteBuffer buffer) {
		if (dataSender == null) {
			throw new HttpException("Request never sent, data cannot be sent");
		} else {
			dataSender.send(buffer);
		}
	}
	
	@Override
	public void endData() {
		if (dataSender == null) {
			throw new HttpException("Request never sent, data cannot be ended");
		} else {
			dataSender.end();
			dataSender = null;
		}
	}

}
