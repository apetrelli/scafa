package com.github.apetrelli.scafa.http.output;

import com.github.apetrelli.scafa.http.HeaderHolder;
import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.output.DataSender;

public interface DataSenderFactory {

	DataSender create(HeaderHolder holder, AsyncSocket socket);
}
