package com.github.apetrelli.scafa.sync.proto.processor;

import com.github.apetrelli.scafa.proto.io.FlowBuffer;
import com.github.apetrelli.scafa.proto.processor.Handler;

public interface DataHandler extends Handler {

	void onData(FlowBuffer buffer);
}
