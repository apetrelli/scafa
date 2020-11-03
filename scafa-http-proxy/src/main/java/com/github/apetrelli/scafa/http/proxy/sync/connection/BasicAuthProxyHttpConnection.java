package com.github.apetrelli.scafa.http.proxy.sync.connection;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.proxy.HttpConnectRequest;
import com.github.apetrelli.scafa.http.proxy.HttpRequestManipulator;
import com.github.apetrelli.scafa.http.proxy.sync.MappedProxyHttpConnectionFactory;
import com.github.apetrelli.scafa.http.sync.HttpSyncSocket;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.processor.ProcessorFactory;
import com.github.apetrelli.scafa.proto.sync.RunnableStarter;
import com.github.apetrelli.scafa.proto.sync.SyncSocket;
import com.github.apetrelli.scafa.proto.sync.processor.DataHandler;

public class BasicAuthProxyHttpConnection extends AbstractUpstreamProxyHttpConnection {

	private String authString;

	public BasicAuthProxyHttpConnection(MappedProxyHttpConnectionFactory factory,
			ProcessorFactory<DataHandler, SyncSocket> clientProcessorFactory,
			RunnableStarter runnableStarter, SyncSocket sourceChannel,
			HttpSyncSocket<HttpRequest> socket, HostPort destinationSocketAddress, HttpRequestManipulator manipulator,
			String username, String password) {
		super(factory, clientProcessorFactory, runnableStarter, sourceChannel, socket, destinationSocketAddress, manipulator);
		String auth = username + ":" + password;
		authString = "Basic " + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.ISO_8859_1));
	}

	@Override
	protected void doConnect(HttpConnectRequest request) {
		request.addHeader("Proxy-Authorization", authString);
		super.doConnect(request);
	}

	@Override
	protected void doSendHeader(HttpRequest request) {
		request.addHeader("Proxy-Authorization", authString);
		super.doSendHeader(request);
	}
}
