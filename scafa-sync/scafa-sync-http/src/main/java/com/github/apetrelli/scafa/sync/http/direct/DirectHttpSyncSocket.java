package com.github.apetrelli.scafa.sync.http.direct;

import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.http.HeaderHolder;
import com.github.apetrelli.scafa.http.HttpException;
import com.github.apetrelli.scafa.sync.http.HttpSyncSocket;
import com.github.apetrelli.scafa.sync.http.output.DataSender;
import com.github.apetrelli.scafa.sync.http.output.DataSenderFactory;
import com.github.apetrelli.scafa.sync.http.output.impl.DirectDataSender;
import com.github.apetrelli.scafa.sync.proto.SyncSocket;
import com.github.apetrelli.scafa.sync.proto.SyncSocketWrapper;

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
	public void sendHeader(H holder, ByteBuffer buffer) {
		dataSender = dataSenderFactory.create(holder, socket);
		holder.fill(buffer);
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
