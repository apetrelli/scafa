package com.github.apetrelli.scafa.http.sync.output;

import com.github.apetrelli.scafa.http.HeaderHolder;
import com.github.apetrelli.scafa.proto.sync.SyncSocket;

public interface DataSenderFactory {

	DataSender create(HeaderHolder holder, SyncSocket socket);
}
