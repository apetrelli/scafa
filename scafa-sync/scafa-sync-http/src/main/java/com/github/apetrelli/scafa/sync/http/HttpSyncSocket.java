package com.github.apetrelli.scafa.sync.http;

import com.github.apetrelli.scafa.http.HeaderHolder;
import com.github.apetrelli.scafa.proto.Socket;
import com.github.apetrelli.scafa.proto.io.FlowBuffer;

public interface HttpSyncSocket<H extends HeaderHolder> extends Socket {

	void sendHeader(H holder);
	
	void sendData(FlowBuffer buffer);

	void endData();
}
