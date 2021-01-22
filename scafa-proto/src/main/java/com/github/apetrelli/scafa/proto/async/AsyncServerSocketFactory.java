package com.github.apetrelli.scafa.proto.async;

import java.io.IOException;

public interface AsyncServerSocketFactory<T extends AsyncSocket> {
	
	AsyncServerSocket<T> create() throws IOException;
}
