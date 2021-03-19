package com.github.apetrelli.scafa.async.proto.socket;

import java.io.IOException;

public interface AsyncServerSocketFactory<T extends AsyncSocket> {
	
	AsyncServerSocket<T> create() throws IOException;
}
