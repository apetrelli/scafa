package com.github.apetrelli.scafa.http.proxy.impl;

import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.proxy.HttpConnectRequest;
import com.github.apetrelli.scafa.http.proxy.HttpRequestManipulator;
import com.github.apetrelli.scafa.http.proxy.MappedProxyHttpConnectionFactory;
import com.github.apetrelli.scafa.proto.client.HostPort;

public class BasicAuthProxyHttpConnection extends AbstractUpstreamProxyHttpConnection {

    private String authString;

    public BasicAuthProxyHttpConnection(MappedProxyHttpConnectionFactory factory, AsynchronousSocketChannel sourceChannel,
            HostPort calledAddress, String interfaceName, boolean forceIpV4, HttpRequestManipulator manipulator, String username, String password) {
        super(factory, sourceChannel, calledAddress, interfaceName, forceIpV4, manipulator);
        String auth = username + ":" + password;
        authString = "Basic " + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.ISO_8859_1));
    }

    @Override
    protected void doConnect(HttpConnectRequest request, CompletionHandler<Void, Void> completionHandler) {
        request.addHeader("Proxy-Authorization", authString);
        super.doConnect(request, completionHandler);
    }

    @Override
    protected void doSendHeader(HttpRequest request, CompletionHandler<Void, Void> completionHandler) {
        request.addHeader("Proxy-Authorization", authString);
        super.doSendHeader(request, completionHandler);
    }
}
