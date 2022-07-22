package com.github.apetrelli.scafa.sync.http.output;

import com.github.apetrelli.scafa.http.HttpConversation;
import com.github.apetrelli.scafa.proto.Socket;

public interface DataSenderFactory {

	DataSender create(HttpConversation holder, Socket socket);
}
