package com.github.apetrelli.scafa.http.client.impl.internal;

import com.github.apetrelli.scafa.http.client.HttpClientConnection;

public class ChunkedDataSenderFactory implements DataSenderFactory {

	@Override
	public DataSender create(HttpClientConnection connection) {
		return new ChunkedDataSender(connection);
	}

}
