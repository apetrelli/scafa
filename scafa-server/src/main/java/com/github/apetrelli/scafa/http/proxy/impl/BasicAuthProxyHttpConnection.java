package com.github.apetrelli.scafa.http.proxy.impl;

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpRequestManipulator;
import com.github.apetrelli.scafa.http.impl.HostPort;
import com.github.apetrelli.scafa.http.proxy.HttpConnectRequest;
import com.github.apetrelli.scafa.http.proxy.MappedHttpConnectionFactory;

public class BasicAuthProxyHttpConnection extends AbstractProxyHttpConnection {
    
    private String authString;

    public BasicAuthProxyHttpConnection(AsynchronousSocketChannel sourceChannel, MappedHttpConnectionFactory factory,
            HostPort calledAddress, String host, int port, HttpRequestManipulator manipulator, String username, String password) throws IOException {
        super(sourceChannel, host, port, manipulator);
        String auth = username + ":" + password;
        authString = "Basic " + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.ISO_8859_1));
        prepareChannel(factory, sourceChannel, calledAddress);
    }

    @Override
    protected void doConnect(HttpConnectRequest request) throws IOException {
        request.addHeader("PROXY-AUTHORIZATION", authString);
        super.doConnect(request);
    }
    
    @Override
    protected void doSendHeader(HttpRequest request) throws IOException {
        request.addHeader("PROXY-AUTHORIZATION", authString);
        super.doSendHeader(request);
    }
}
