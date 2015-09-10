/**
 * Scafa - A universal non-caching proxy for the road warrior
 * Copyright (C) 2015  Antonio Petrelli
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.apetrelli.scafa.http.ntlm;

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;

import com.github.apetrelli.scafa.http.HttpByteSink;
import com.github.apetrelli.scafa.http.HttpHandler;
import com.github.apetrelli.scafa.http.HttpInput;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpRequestManipulator;
import com.github.apetrelli.scafa.http.HttpStatus;
import com.github.apetrelli.scafa.http.impl.DefaultHttpByteSink;
import com.github.apetrelli.scafa.http.impl.HostPort;
import com.github.apetrelli.scafa.http.proxy.HttpConnectRequest;
import com.github.apetrelli.scafa.http.proxy.MappedHttpConnectionFactory;
import com.github.apetrelli.scafa.http.proxy.impl.AbstractProxyHttpConnection;
import com.github.apetrelli.scafa.processor.BufferProcessor;
import com.github.apetrelli.scafa.processor.impl.ClientBufferProcessor;
import com.github.apetrelli.scafa.server.Status;
import com.github.apetrelli.scafa.util.HttpUtils;

import jcifs.ntlmssp.NtlmFlags;
import jcifs.ntlmssp.Type1Message;
import jcifs.ntlmssp.Type2Message;
import jcifs.ntlmssp.Type3Message;
import jcifs.util.Base64;

public class NtlmProxyHttpConnection extends AbstractProxyHttpConnection {

    private static final int TYPE_1_FLAGS = NtlmFlags.NTLMSSP_NEGOTIATE_128 | NtlmFlags.NTLMSSP_NEGOTIATE_ALWAYS_SIGN
            | NtlmFlags.NTLMSSP_NEGOTIATE_LM_KEY | NtlmFlags.NTLMSSP_NEGOTIATE_TARGET_INFO
            | NtlmFlags.NTLMSSP_NEGOTIATE_OEM | NtlmFlags.NTLMSSP_NEGOTIATE_UNICODE;

    private boolean authenticated = false;

    private MappedHttpConnectionFactory factory;

    private HostPort calledAddress;

    private String domain, username, password;

    private TentativeHandler tentativeHandler;

    public NtlmProxyHttpConnection(AsynchronousSocketChannel sourceChannel, MappedHttpConnectionFactory factory,
            HostPort calledAddress, String host, int port, String domain, String username, String password,
            HttpRequestManipulator manipulator) throws IOException {
        super(sourceChannel, host, port, manipulator);
        this.factory = factory;
        this.calledAddress = calledAddress;
        this.domain = domain;
        this.username = username;
        this.password = password;
        tentativeHandler = new TentativeHandler(sourceChannel);
    }

    @Override
    protected void doSendHeader(HttpRequest request) throws IOException {
        if (!authenticated) {
            authenticate(request);
        } else {
            HttpUtils.sendHeader(request, channel);
        }
    }

    @Override
    protected void doConnect(HttpConnectRequest request) throws IOException {
        if (!authenticated) {
            authenticateOnConnect(request);
        } else {
            HttpUtils.sendHeader(request, channel);
        }
    }

    private void authenticateOnConnect(HttpRequest request) throws IOException {
        HttpRequest modifiedRequest = new HttpRequest(request);
        modifiedRequest.setHeader("PROXY-CONNECTION", "keep-alive");
        HttpByteSink sink = new DefaultHttpByteSink<HttpHandler>(tentativeHandler);
        BufferProcessor<HttpInput, HttpByteSink> processor = new ClientBufferProcessor<>(sink);
        ntlmAuthenticate(modifiedRequest, modifiedRequest, sink, tentativeHandler, processor);
    }

    private void authenticate(HttpRequest request) throws IOException {
        HttpRequest finalRequest = new HttpRequest(request);
        finalRequest.setHeader("PROXY-CONNECTION", "keep-alive");
        HttpRequest modifiedRequest = new HttpRequest(finalRequest);
        String length = request.getHeader("CONTENT-LENGTH");
        if (length != null) {
            modifiedRequest.setHeader("CONTENT-LENGTH", "0");
        }
        if (HttpUtils.sendHeader(modifiedRequest, channel) >= 0) {
            HttpByteSink sink = new DefaultHttpByteSink<HttpHandler>(tentativeHandler);
            BufferProcessor<HttpInput, HttpByteSink> processor = new ClientBufferProcessor<>(sink);
            if (readResponse(tentativeHandler, sink, processor) >= 0) {
                if (tentativeHandler.isNeedsAuthorizing()) {
                    tentativeHandler.setOnlyCaptureMode(true);
                    if (tentativeHandler.getResponse().getHeaders("PROXY-AUTHENTICATE").contains("NTLM")) {
                        ntlmAuthenticate(modifiedRequest, finalRequest, sink, tentativeHandler, processor);
                    }
                } else {
                    authenticated = true;
                    prepareChannel(factory, sourceChannel, calledAddress);
                }
            } else {
                throw new IOException("Connection closed");
            }
        }
    }

    private void ntlmAuthenticate(HttpRequest modifiedRequest, HttpRequest finalRequest, HttpByteSink sink, CapturingHandler handler,
            BufferProcessor<HttpInput, HttpByteSink> processor) throws IOException {
        Type1Message message1 = new Type1Message(TYPE_1_FLAGS, null, null);
        modifiedRequest.setHeader("PROXY-AUTHORIZATION", "NTLM " + Base64.encode(message1.toByteArray()));
        if (HttpUtils.sendHeader(modifiedRequest, channel) >= 0) {
            if (readResponse(handler, sink, processor) >= 0) {
                switch (handler.getResponse().getCode()) {
                case 407:
                    String authenticate = handler.getResponse().getHeader("PROXY-AUTHENTICATE");
                    if (authenticate != null) {
                        if (authenticate.startsWith("NTLM ")) {
                            String base64 = authenticate.substring(5);
                            Type2Message message2 = new Type2Message(Base64.decode(base64));
                            Type3Message message3 = new Type3Message(message2, password, domain, username, null,
                                    message2.getFlags());
                            finalRequest.setHeader("PROXY-AUTHORIZATION",
                                    "NTLM " + Base64.encode(message3.toByteArray()));
                            HttpUtils.sendHeader(finalRequest, channel);
                            authenticated = true;
                            prepareChannel(factory, sourceChannel, calledAddress);
                        }
                    }
                    break;
                case 200:
                    authenticated = true;
                    prepareChannel(factory, sourceChannel, calledAddress);
                    break;
                default:
                    // this happens only in HTTP with disallowed connections.
                    channel.close();
                }
            }
        }
    }

    private Integer readResponse(CapturingHandler handler, HttpByteSink sink,
            BufferProcessor<HttpInput, HttpByteSink> processor) throws IOException {
        handler.reset();
        Integer retValue = 0;
        sink.reset();
        HttpInput input = sink.createInput();
        input.setBuffer(readBuffer);
        Status<HttpInput, HttpByteSink> status = HttpStatus.IDLE;
        while (!handler.isFinished()) {
            readBuffer.clear();
            retValue = HttpUtils.getFuture(channel.read(readBuffer));
            readBuffer.flip();
            status = processor.process(input, status);
        }
        readBuffer.clear();
        return retValue;
    }
}
