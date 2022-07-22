package com.github.apetrelli.scafa.http2;

import com.github.apetrelli.scafa.http.HeaderHolder;
import com.github.apetrelli.scafa.proto.Socket;
import com.github.apetrelli.scafa.proto.io.FlowBuffer;
import com.github.apetrelli.scafa.sync.http.HttpSyncSocket;
import com.github.apetrelli.scafa.sync.proto.SyncSocketWrapper;

public class HttpStreamSocket extends SyncSocketWrapper<Socket> implements HttpSyncSocket<HeaderHolder> {
	
	private final Stream stream;
	
	public HttpStreamSocket(Socket socket, Stream stream) {
		super(socket);
		this.stream = stream;
	}

	@Override
	public void sendHeader(HeaderHolder holder) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendData(FlowBuffer buffer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void endData() {
		// TODO Auto-generated method stub
		
	}

}
