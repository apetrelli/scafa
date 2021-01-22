package com.github.apetrelli.scafa.proto.async.socket;

import java.io.IOException;

public interface AsyncServerSocketFactory<T extends AsyncSocket> {
	
	AsyncServerSocket<T> create() throws IOException;
}
