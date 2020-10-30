package com.github.apetrelli.scafa.http.sync;

import java.nio.ByteBuffer;

import com.github.apetrelli.scafa.http.HeaderHolder;
import com.github.apetrelli.scafa.proto.sync.SyncSocket;

public interface HttpSyncSocket<H extends HeaderHolder> extends SyncSocket {

	void sendHeader(H holder);
	
	void sendData(ByteBuffer buffer);

	void endData();
}
