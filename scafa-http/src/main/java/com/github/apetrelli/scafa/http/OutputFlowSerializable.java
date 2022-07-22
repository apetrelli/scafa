package com.github.apetrelli.scafa.http;

import com.github.apetrelli.scafa.proto.io.OutputFlow;

public interface OutputFlowSerializable {

	void fill(OutputFlow out);
}
