package com.github.apetrelli.scafa.sync.http.socket.direct;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.http.HttpConversation;
import com.github.apetrelli.scafa.http.HttpException;
import com.github.apetrelli.scafa.proto.Socket;
import com.github.apetrelli.scafa.proto.io.FlowBuffer;
import com.github.apetrelli.scafa.sync.http.HttpSyncSocket;
import com.github.apetrelli.scafa.sync.http.output.DataSender;
import com.github.apetrelli.scafa.sync.http.output.DataSenderFactory;
import com.github.apetrelli.scafa.sync.http.output.impl.DirectDataSender;
import com.github.apetrelli.scafa.sync.proto.SyncSocketWrapper;

public class DirectHttpSyncSocket<H extends HttpConversation> extends SyncSocketWrapper<Socket> implements HttpSyncSocket<H> {

	private static final Logger LOG = Logger.getLogger(DirectHttpSyncSocket.class.getName());
	
	private DataSender dataSender;
	
	private final DataSenderFactory dataSenderFactory;
	
	public DirectHttpSyncSocket(Socket socket, DataSenderFactory dataSenderFactory) {
		super(socket);
		dataSender = new DirectDataSender(socket);
		this.dataSenderFactory = dataSenderFactory;
	}
	
	@Override
	public void sendHeader(H holder) {
		dataSender = dataSenderFactory.create(holder, socket);
		holder.fill(socket.out());
		socket.out().flush();
		if (LOG.isLoggable(Level.FINEST)) {
			String request = holder.toString();
			LOG.finest("-- Raw request/response header");
			LOG.finest(request);
			LOG.finest("-- End of header --");
		}
	}

	@Override
	public void sendData(FlowBuffer buffer) {
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
