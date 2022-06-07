package com.github.apetrelli.scafa.sync.http.output;

import com.github.apetrelli.scafa.http.HeaderHolder;
import com.github.apetrelli.scafa.sync.proto.SyncSocket;

public interface DataSenderFactory {

	DataSender create(HeaderHolder holder, SyncSocket socket);
}
