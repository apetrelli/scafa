package com.github.apetrelli.scafa.sync.http.output;

import com.github.apetrelli.scafa.http.HeaderHolder;
import com.github.apetrelli.scafa.proto.Socket;

public interface DataSenderFactory {

	DataSender create(HeaderHolder holder, Socket socket);
}
