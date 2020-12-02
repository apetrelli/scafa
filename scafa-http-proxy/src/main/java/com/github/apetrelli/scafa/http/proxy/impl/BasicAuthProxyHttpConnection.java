package com.github.apetrelli.scafa.http.proxy.impl;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.http.HeaderName;
import com.github.apetrelli.scafa.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.gateway.MappedGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.http.proxy.HttpConnectRequest;
import com.github.apetrelli.scafa.http.proxy.HttpRequestManipulator;
import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.processor.DataHandler;
import com.github.apetrelli.scafa.proto.processor.ProcessorFactory;
import com.github.apetrelli.scafa.proto.util.AsciiString;

public class BasicAuthProxyHttpConnection extends AbstractUpstreamProxyHttpConnection {

    private static final HeaderName PROXY_AUTHORIZATION = new HeaderName("Proxy-Authorization");
    
	private AsciiString authString;

	public BasicAuthProxyHttpConnection(MappedGatewayHttpConnectionFactory<?> factory,
			ProcessorFactory<DataHandler, AsyncSocket> clientProcessorFactory, AsyncSocket sourceChannel,
			HttpAsyncSocket<HttpRequest> socket, HostPort destinationSocketAddress, HttpRequestManipulator manipulator,
			String username, String password) {
        super(factory, clientProcessorFactory, sourceChannel, socket, destinationSocketAddress, manipulator);
        String auth = username + ":" + password;
        authString = new AsciiString("Basic " + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.ISO_8859_1)));
    }

    @Override
    protected CompletableFuture<Void> doConnect(HttpConnectRequest request) {
        request.addHeader(PROXY_AUTHORIZATION, authString);
        return super.doConnect(request);
    }
    
    @Override
    protected CompletableFuture<Void> doSendHeader(HttpRequest request) {
        request.addHeader(PROXY_AUTHORIZATION, authString);
        return super.doSendHeader(request);
    }
}
