package com.github.apetrelli.scafa.sync.http;

import java.nio.ByteBuffer;

import com.github.apetrelli.scafa.http.HeaderHolder;
import com.github.apetrelli.scafa.sync.proto.SyncSocket;

public interface HttpSyncSocket<H extends HeaderHolder> extends SyncSocket {

	void sendHeader(H holder, ByteBuffer buffer);
	
	void sendData(ByteBuffer buffer);

	void endData();
}
