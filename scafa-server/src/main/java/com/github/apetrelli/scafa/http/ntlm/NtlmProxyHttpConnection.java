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
import java.nio.channels.CompletionHandler;

import com.github.apetrelli.scafa.http.HostPort;
import com.github.apetrelli.scafa.http.HttpByteSink;
import com.github.apetrelli.scafa.http.HttpHandler;
import com.github.apetrelli.scafa.http.HttpInput;
import com.github.apetrelli.scafa.http.HttpProcessingContext;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpStatus;
import com.github.apetrelli.scafa.http.impl.DefaultHttpByteSink;
import com.github.apetrelli.scafa.http.impl.HttpInputProcessor;
import com.github.apetrelli.scafa.http.impl.HttpStateMachine;
import com.github.apetrelli.scafa.http.proxy.HttpConnectRequest;
import com.github.apetrelli.scafa.http.proxy.HttpRequestManipulator;
import com.github.apetrelli.scafa.http.proxy.MappedHttpConnectionFactory;
import com.github.apetrelli.scafa.http.proxy.impl.AbstractUpstreamProxyHttpConnection;
import com.github.apetrelli.scafa.proto.aio.DelegateFailureCompletionHandler;
import com.github.apetrelli.scafa.util.HttpUtils;

import jcifs.ntlmssp.NtlmFlags;
import jcifs.ntlmssp.Type1Message;
import jcifs.ntlmssp.Type2Message;
import jcifs.ntlmssp.Type3Message;
import jcifs.util.Base64;

public class NtlmProxyHttpConnection extends AbstractUpstreamProxyHttpConnection {

    private static final int TYPE_1_FLAGS = NtlmFlags.NTLMSSP_NEGOTIATE_128 | NtlmFlags.NTLMSSP_NEGOTIATE_ALWAYS_SIGN
            | NtlmFlags.NTLMSSP_NEGOTIATE_LM_KEY | NtlmFlags.NTLMSSP_NEGOTIATE_TARGET_INFO
            | NtlmFlags.NTLMSSP_NEGOTIATE_OEM | NtlmFlags.NTLMSSP_NEGOTIATE_UNICODE;

    private boolean authenticated = false;

    private MappedHttpConnectionFactory factory;

    private HostPort calledAddress;

    private String domain, username, password;

    private HttpStateMachine stateMachine;

    private TentativeHandler tentativeHandler;

    public NtlmProxyHttpConnection(MappedHttpConnectionFactory factory, AsynchronousSocketChannel sourceChannel,
            HostPort calledAddress, String interfaceName, boolean forceIpV4, HostPort proxySocketAddress, String domain, String username, String password,
            HttpStateMachine stateMachine, HttpRequestManipulator manipulator) {
        super(factory, sourceChannel, calledAddress, interfaceName, forceIpV4, proxySocketAddress, manipulator);
        this.factory = factory;
        this.calledAddress = calledAddress;
        this.domain = domain;
        this.username = username;
        this.password = password;
        this.stateMachine = stateMachine;
        tentativeHandler = new TentativeHandler(sourceChannel);
    }

    @Override
    protected void doSendHeader(HttpRequest request, CompletionHandler<Void, Void> completionHandler) {
        if (!authenticated) {
            authenticate(request, completionHandler);
        } else {
            HttpUtils.sendHeader(request, channel, completionHandler);
        }
    }

    @Override
    protected void doConnect(HttpConnectRequest request, CompletionHandler<Void, Void> completionHandler) {
        if (!authenticated) {
            authenticateOnConnect(request, completionHandler);
        } else {
            HttpUtils.sendHeader(request, channel, completionHandler);
        }
    }

    private void authenticateOnConnect(HttpRequest request, CompletionHandler<Void, Void> completionHandler) {
        HttpRequest modifiedRequest = new HttpRequest(request);
        modifiedRequest.setHeader("Proxy-Connection", "keep-alive");
        HttpByteSink sink = new DefaultHttpByteSink<HttpHandler>(tentativeHandler);
        HttpInputProcessor processor = new HttpInputProcessor(sink, tentativeHandler, stateMachine);
        ntlmAuthenticate(modifiedRequest, modifiedRequest, sink, tentativeHandler, processor, completionHandler);
    }

    private void authenticate(HttpRequest request, CompletionHandler<Void, Void> completionHandler) {
        HttpRequest finalRequest = new HttpRequest(request);
        finalRequest.setHeader("Proxy-Connection", "keep-alive");
        HttpRequest modifiedRequest = new HttpRequest(finalRequest);
        String length = request.getHeader("CONTENT-LENGTH");
        if (length != null) {
            modifiedRequest.setHeader("Content-Length", "0");
        }
        HttpUtils.sendHeader(modifiedRequest, channel, new DelegateFailureCompletionHandler<Void, Void>(completionHandler) {

            @Override
            public void completed(Void result, Void attachment) {
                HttpByteSink sink = new DefaultHttpByteSink<HttpHandler>(tentativeHandler);
                HttpInputProcessor processor = new HttpInputProcessor(sink, tentativeHandler, stateMachine);
                readResponse(tentativeHandler, sink, processor, new DelegateFailureCompletionHandler<Integer, Void>(completionHandler) {

                    @Override
                    public void completed(Integer result, Void attachment) {
                        if (result >= 0) {
                            if (tentativeHandler.isNeedsAuthorizing()) {
                                tentativeHandler.setOnlyCaptureMode(true);
                                if (tentativeHandler.getResponse().getHeaders("PROXY-AUTHENTICATE").contains("NTLM")) {
                                    ntlmAuthenticate(modifiedRequest, finalRequest, sink, tentativeHandler, processor, completionHandler);
                                }
                            } else {
                                authenticated = true;
                                prepareChannel(factory, sourceChannel, calledAddress);
                                completionHandler.completed(null, null);
                            }
                        } else {
                            completionHandler.failed(new IOException("Connection closed"), null);
                        }
                    }
                });
            }
        });
    }

    private void ntlmAuthenticate(HttpRequest modifiedRequest, HttpRequest finalRequest, HttpByteSink sink, CapturingHandler handler,
            HttpInputProcessor processor, CompletionHandler<Void, Void> completionHandler) {
        Type1Message message1 = new Type1Message(TYPE_1_FLAGS, null, null);
        modifiedRequest.setHeader("PROXY-AUTHORIZATION", "NTLM " + Base64.encode(message1.toByteArray()));
        HttpUtils.sendHeader(modifiedRequest, channel, new DelegateFailureCompletionHandler<Void, Void>(completionHandler) {

            @Override
            public void completed(Void result, Void attachment) {
                readResponse(handler, sink, processor, new DelegateFailureCompletionHandler<Integer, Void>(completionHandler) {

                    @Override
                    public void completed(Integer result, Void attachment) {
                        if (result >= 0) {
                            try {
                                switch (handler.getResponse().getCode()) {
                                case 407:
                                    String authenticate = handler.getResponse().getHeader("PROXY-AUTHENTICATE");
                                    if (authenticate != null) {
                                        if (authenticate.startsWith("NTLM ")) {
                                            String base64 = authenticate.substring(5);
                                            Type2Message message2 = new Type2Message(Base64.decode(base64));
                                            Type3Message message3 = new Type3Message(message2, password, domain, username, null,
                                                    message2.getFlags());
                                            finalRequest.setHeader("Proxy-Authorization",
                                                    "NTLM " + Base64.encode(message3.toByteArray()));
                                            HttpUtils.sendHeader(finalRequest, channel, new DelegateFailureCompletionHandler<Void, Void>(completionHandler) {

                                                @Override
                                                public void completed(Void result, Void attachment) {
                                                    authenticated = true;
                                                    prepareChannel(factory, sourceChannel, calledAddress);
                                                    completionHandler.completed(null, null);
                                                }
                                            });
                                        } else {
                                            completionHandler.completed(null, null);
                                        }
                                    } else {
                                        completionHandler.completed(null, null);
                                    }
                                    break;
                                case 200:
                                    authenticated = true;
                                    prepareChannel(factory, sourceChannel, calledAddress);
                                    completionHandler.completed(null, null);
                                    break;
                                default:
                                    // this happens only in HTTP with disallowed connections.
                                    channel.close();
                                    completionHandler.completed(null, null);
                                }
                            } catch (IOException e) {
                                completionHandler.failed(e, null);
                            }
                        } else {
                            completionHandler.failed(new IOException("Connection closed"), null);
                        }
                    }
                });
            }
        });
    }

    private void readResponse(CapturingHandler handler, HttpByteSink sink,
            HttpInputProcessor processor, CompletionHandler<Integer, Void> completionHandler) {
        handler.reset();
        sink.reset();
        HttpInput input = sink.createInput();
        input.setBuffer(readBuffer);
        HttpProcessingContext context = new HttpProcessingContext(HttpStatus.IDLE, input);
        Integer retValue = 0;
        readResponse(handler, processor, completionHandler, context, retValue);
    }

    private void readResponse(CapturingHandler handler, HttpInputProcessor processor,
            CompletionHandler<Integer, Void> completionHandler, HttpProcessingContext context,
            Integer byteRead) {
        if (!handler.isFinished()) {
            readBuffer.clear();
            channel.read(readBuffer, byteRead, new DelegateFailureCompletionHandler<Integer, Integer>(completionHandler) {

                @Override
                public void completed(Integer result, Integer attachment) {
                    Integer newByteRead = result + attachment;
                    readBuffer.flip();
                    processor.process(context, new CompletionHandler<HttpProcessingContext, HttpProcessingContext>() {

                        @Override
                        public void completed(HttpProcessingContext result,
                                HttpProcessingContext attachment) {
                            readResponse(handler, processor, completionHandler, context, newByteRead);
                        }

                        @Override
                        public void failed(Throwable exc, HttpProcessingContext attachment) {
                            completionHandler.failed(exc, null);
                        }
                    });
                }
            });
        } else {
            completionHandler.completed(byteRead, null);
        }
    }
}
