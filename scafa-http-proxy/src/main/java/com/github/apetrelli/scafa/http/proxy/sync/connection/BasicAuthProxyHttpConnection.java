package com.github.apetrelli.scafa.http.proxy.sync.connection;

import static com.github.apetrelli.scafa.http.HttpHeaders.PROXY_AUTHORIZATION;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.gateway.sync.MappedGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.http.proxy.HttpConnectRequest;
import com.github.apetrelli.scafa.http.proxy.HttpRequestManipulator;
import com.github.apetrelli.scafa.sync.http.HttpSyncSocket;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.processor.ProcessorFactory;
import com.github.apetrelli.scafa.proto.util.AsciiString;
import com.github.apetrelli.scafa.sync.proto.RunnableStarter;
import com.github.apetrelli.scafa.sync.proto.SyncSocket;
import com.github.apetrelli.scafa.sync.proto.processor.DataHandler;

public class BasicAuthProxyHttpConnection extends AbstractUpstreamProxyHttpConnection {

	private AsciiString authString;

	public BasicAuthProxyHttpConnection(MappedGatewayHttpConnectionFactory<?> factory,
			ProcessorFactory<DataHandler, SyncSocket> clientProcessorFactory,
			RunnableStarter runnableStarter, SyncSocket sourceChannel,
			HttpSyncSocket<HttpRequest> socket, HostPort destinationSocketAddress, HttpRequestManipulator manipulator,
			String username, String password) {
		super(factory, clientProcessorFactory, runnableStarter, sourceChannel, socket, destinationSocketAddress, manipulator);
		String auth = username + ":" + password;
		authString = new AsciiString("Basic " + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.ISO_8859_1)));
	}

	@Override
	protected void doConnect(HttpConnectRequest request, ByteBuffer buffer) {
		request.addHeader(PROXY_AUTHORIZATION, authString);
		super.doConnect(request, buffer);
	}

	@Override
	protected void doSendHeader(HttpRequest request, ByteBuffer buffer) {
		request.addHeader(PROXY_AUTHORIZATION, authString);
		super.doSendHeader(request, buffer);
	}
}
