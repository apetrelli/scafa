package com.github.apetrelli.scafa.proto.aio;

import java.io.IOException;

public interface AsyncServerSocketFactory<T extends AsyncSocket> {
	
	AsyncServerSocket<T> create() throws IOException;
}
