package com.github.apetrelli.scafa.http.proxy.impl;

import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpRequestManipulator;
import com.github.apetrelli.scafa.http.impl.HostPort;
import com.github.apetrelli.scafa.http.proxy.HttpConnectRequest;
import com.github.apetrelli.scafa.http.proxy.MappedHttpConnectionFactory;

public class BasicAuthProxyHttpConnection extends AbstractProxyHttpConnection {

    private String authString;

    public BasicAuthProxyHttpConnection(MappedHttpConnectionFactory factory, AsynchronousSocketChannel sourceChannel,
            HostPort calledAddress, String host, int port, HttpRequestManipulator manipulator, String username, String password) {
        super(factory, sourceChannel, calledAddress, host, port, manipulator);
        String auth = username + ":" + password;
        authString = "Basic " + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.ISO_8859_1));
    }

    @Override
    protected void doConnect(HttpConnectRequest request, CompletionHandler<Void, Void> completionHandler) {
        request.addHeader("PROXY-AUTHORIZATION", authString);
        super.doConnect(request, completionHandler);
    }

    @Override
    protected void doSendHeader(HttpRequest request, CompletionHandler<Void, Void> completionHandler) {
        request.addHeader("PROXY-AUTHORIZATION", authString);
        super.doSendHeader(request, completionHandler);
    }
}
