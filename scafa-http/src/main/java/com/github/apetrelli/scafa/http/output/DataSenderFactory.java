package com.github.apetrelli.scafa.http.output;

import com.github.apetrelli.scafa.http.HeaderHolder;
import com.github.apetrelli.scafa.proto.async.AsyncSocket;

public interface DataSenderFactory {

	DataSender create(HeaderHolder holder, AsyncSocket socket);
}
