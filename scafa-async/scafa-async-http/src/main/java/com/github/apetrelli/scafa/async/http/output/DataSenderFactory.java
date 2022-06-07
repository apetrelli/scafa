package com.github.apetrelli.scafa.async.http.output;

import com.github.apetrelli.scafa.async.proto.socket.AsyncSocket;
import com.github.apetrelli.scafa.http.HeaderHolder;

public interface DataSenderFactory {

	DataSender create(HeaderHolder holder, AsyncSocket socket);
}
