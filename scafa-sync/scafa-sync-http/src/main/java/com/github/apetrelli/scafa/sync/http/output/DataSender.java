package com.github.apetrelli.scafa.sync.http.output;

import com.github.apetrelli.scafa.proto.io.FlowBuffer;

public interface DataSender {

	void send(FlowBuffer buffer);

	void end();
}
