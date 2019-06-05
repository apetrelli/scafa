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

    public NtlmProxyHttpConnection(MappedHttpConnectionFactory factory, AsynchronousSocketChannel sourceChannel,
            HostPort calledAddress, String host, int port, String domain, String username, String password,
            HttpRequestManipulator manipulator) {
        super(factory, sourceChannel, calledAddress, host, port, manipulator);
        this.factory = factory;
        this.calledAddress = calledAddress;
        this.domain = domain;
        this.username = username;
        this.password = password;
        tentativeHandler = new TentativeHandler(sourceChannel);
    }

    @Override
    protected void doSendHeader(HttpRequest request, CompletionHandler<Void, Void> completionHandler) {
        if (!authenticated) {
            authenticate(request, completionHandler);
        } else {
            try {
				HttpUtils.sendHeader(request, channel);
				completionHandler.completed(null, null);
			} catch (IOException e) {
				completionHandler.failed(e, null);
			}
        }
    }

    @Override
    protected void doConnect(HttpConnectRequest request, CompletionHandler<Void, Void> completionHandler) {
        if (!authenticated) {
            authenticateOnConnect(request, completionHandler);
        } else {
            try {
				HttpUtils.sendHeader(request, channel);
				completionHandler.completed(null, null);
			} catch (IOException e) {
				completionHandler.failed(e, null);
			}
        }
    }

    private void authenticateOnConnect(HttpRequest request, CompletionHandler<Void, Void> completionHandler) {
        HttpRequest modifiedRequest = new HttpRequest(request);
        modifiedRequest.setHeader("PROXY-CONNECTION", "keep-alive");
        HttpByteSink sink = new DefaultHttpByteSink<HttpHandler>(tentativeHandler);
        BufferProcessor<HttpInput, HttpByteSink> processor = new ClientBufferProcessor<>(sink);
        ntlmAuthenticate(modifiedRequest, modifiedRequest, sink, tentativeHandler, processor, completionHandler);
    }

    private void authenticate(HttpRequest request, CompletionHandler<Void, Void> completionHandler) {
        HttpRequest finalRequest = new HttpRequest(request);
        finalRequest.setHeader("PROXY-CONNECTION", "keep-alive");
        HttpRequest modifiedRequest = new HttpRequest(finalRequest);
        String length = request.getHeader("CONTENT-LENGTH");
        if (length != null) {
            modifiedRequest.setHeader("CONTENT-LENGTH", "0");
        }
        try {
			if (HttpUtils.sendHeader(modifiedRequest, channel) >= 0) {
			    HttpByteSink sink = new DefaultHttpByteSink<HttpHandler>(tentativeHandler);
			    BufferProcessor<HttpInput, HttpByteSink> processor = new ClientBufferProcessor<>(sink);
			    readResponse(tentativeHandler, sink, processor, new CompletionHandler<Integer, Void>() {

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

					@Override
					public void failed(Throwable exc, Void attachment) {
						completionHandler.failed(exc, attachment);
					}
				});
			}
		} catch (IOException e) {
			completionHandler.failed(e, null);
		}
    }

    private void ntlmAuthenticate(HttpRequest modifiedRequest, HttpRequest finalRequest, HttpByteSink sink, CapturingHandler handler,
            BufferProcessor<HttpInput, HttpByteSink> processor, CompletionHandler<Void, Void> completionHandler) {
        Type1Message message1 = new Type1Message(TYPE_1_FLAGS, null, null);
        modifiedRequest.setHeader("PROXY-AUTHORIZATION", "NTLM " + Base64.encode(message1.toByteArray()));
        try {
			if (HttpUtils.sendHeader(modifiedRequest, channel) >= 0) {
				readResponse(handler, sink, processor, new CompletionHandler<Integer, Void>() {

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
				                completionHandler.completed(null, null);
							} catch (IOException e) {
								completionHandler.failed(e, null);
							}
						} else {
							completionHandler.failed(new IOException("Connection closed"), null);
						}
					}

					@Override
					public void failed(Throwable exc, Void attachment) {
						completionHandler.failed(exc, attachment);
					}
				});
			}
		} catch (IOException e) {
			completionHandler.failed(e, null);
		}
    }

	private void readResponse(CapturingHandler handler, HttpByteSink sink,
			BufferProcessor<HttpInput, HttpByteSink> processor, CompletionHandler<Integer, Void> completionHandler) {
        handler.reset();
        sink.reset();
        HttpInput input = sink.createInput();
        input.setBuffer(readBuffer);
        Status<HttpInput, HttpByteSink> status = HttpStatus.IDLE;
        Integer retValue = 0;
    	readResponse(handler, processor, completionHandler, input, status, retValue);
    }

	private void readResponse(CapturingHandler handler, BufferProcessor<HttpInput, HttpByteSink> processor,
			CompletionHandler<Integer, Void> completionHandler, HttpInput input, Status<HttpInput, HttpByteSink> status,
			Integer byteRead) {
		if (!handler.isFinished()) {
            readBuffer.clear();
            try {
				Integer newByteRead = byteRead + HttpUtils.getFuture(channel.read(readBuffer));
	            readBuffer.flip();
	            processor.process(input, status, new CompletionHandler<Status<HttpInput,HttpByteSink>, HttpInput>() {

					@Override
					public void completed(Status<HttpInput, HttpByteSink> result, HttpInput attachment) {
						readResponse(handler, processor, completionHandler, input, status, newByteRead);
					}

					@Override
					public void failed(Throwable exc, HttpInput attachment) {
						completionHandler.failed(exc, null);
					}
				});
			} catch (IOException e) {
				completionHandler.failed(e, null);
			}
    	} else {
    		completionHandler.completed(byteRead, null);
    	}
	}
}
