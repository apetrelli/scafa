package com.github.apetrelli.scafa.http.client.impl.internal;

import com.github.apetrelli.scafa.http.client.HttpClientConnection;

public interface DataSenderFactory {

	DataSender create(HttpClientConnection connection);
}
