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
package com.github.apetrelli.scafa.http.proxy.ntlm;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.http.HttpHandler;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.impl.HttpStateMachine;
import com.github.apetrelli.scafa.http.proxy.HttpConnectRequest;
import com.github.apetrelli.scafa.http.proxy.HttpRequestManipulator;
import com.github.apetrelli.scafa.http.proxy.MappedProxyHttpConnectionFactory;
import com.github.apetrelli.scafa.http.proxy.impl.AbstractUpstreamProxyHttpConnection;
import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.aio.CompletionHandlerFuture;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.processor.DataHandler;
import com.github.apetrelli.scafa.proto.processor.ProcessorFactory;
import com.github.apetrelli.scafa.proto.processor.impl.StatefulInputProcessor;

import jcifs.ntlmssp.NtlmFlags;
import jcifs.ntlmssp.Type1Message;
import jcifs.ntlmssp.Type2Message;
import jcifs.ntlmssp.Type3Message;
import jcifs.util.Base64;

public class NtlmProxyHttpConnection extends AbstractUpstreamProxyHttpConnection {

    private static final String NTLM = "NTLM ";

	private static final int TYPE_1_FLAGS = NtlmFlags.NTLMSSP_NEGOTIATE_128 | NtlmFlags.NTLMSSP_NEGOTIATE_ALWAYS_SIGN
            | NtlmFlags.NTLMSSP_NEGOTIATE_LM_KEY | NtlmFlags.NTLMSSP_NEGOTIATE_TARGET_INFO
            | NtlmFlags.NTLMSSP_NEGOTIATE_OEM | NtlmFlags.NTLMSSP_NEGOTIATE_UNICODE;

    private boolean authenticated = false;

    private String domain, username, password;

    private HttpStateMachine stateMachine;

    private TentativeHandler tentativeHandler;

    private NtlmHttpProcessingContextFactory processingContextFactory;

    private ByteBuffer readBuffer = ByteBuffer.allocate(16384);

	public NtlmProxyHttpConnection(MappedProxyHttpConnectionFactory factory,
			ProcessorFactory<DataHandler, AsyncSocket> clientProcessorFactory, HttpAsyncSocket<HttpResponse> sourceChannel,
			HttpAsyncSocket<HttpRequest> socket, HostPort destinationSocketAddress, String domain, String username,
			String password, HttpStateMachine stateMachine, HttpRequestManipulator manipulator) {
        super(factory, clientProcessorFactory, sourceChannel, socket, destinationSocketAddress, manipulator);
        this.domain = domain;
        this.username = username;
        this.password = password;
        this.stateMachine = stateMachine;
        tentativeHandler = new TentativeHandler(sourceChannel);
        processingContextFactory = new NtlmHttpProcessingContextFactory();
    }

    @Override
    protected CompletableFuture<Void> doSendHeader(HttpRequest request) {
        if (!authenticated) {
            return authenticate(request);
        } else {
            return socket.sendHeader(request);
        }
    }
    @Override
    protected CompletableFuture<Void> doConnect(HttpConnectRequest request) {
        if (!authenticated) {
            return authenticateOnConnect(request);
        } else {
            return socket.sendHeader(request);
        }
    }

    private CompletableFuture<Void> authenticateOnConnect(HttpRequest request) {
        HttpRequest modifiedRequest = new HttpRequest(request);
        modifiedRequest.setHeader("Proxy-Connection", "keep-alive");
        StatefulInputProcessor<HttpHandler, NtlmHttpProcessingContext> processor = new StatefulInputProcessor<>(tentativeHandler, stateMachine);
        return ntlmAuthenticate(modifiedRequest, modifiedRequest, tentativeHandler, processor);
    }

    private CompletableFuture<Void> authenticate(HttpRequest request) {
        HttpRequest finalRequest = new HttpRequest(request);
        finalRequest.setHeader("Proxy-Connection", "keep-alive");
        HttpRequest modifiedRequest = new HttpRequest(finalRequest);
        String length = request.getHeader("CONTENT-LENGTH");
        if (length != null) {
            modifiedRequest.setHeader("Content-Length", "0");
        }
        StatefulInputProcessor<HttpHandler, NtlmHttpProcessingContext> processor = new StatefulInputProcessor<>(
                tentativeHandler, stateMachine);
		return socket.sendHeader(modifiedRequest).thenCompose(x -> readResponse(tentativeHandler, processor))
				.thenCompose(x -> {
					if (x >= 0) {
						if (tentativeHandler.isNeedsAuthorizing()) {
							tentativeHandler.setOnlyCaptureMode(true);
							if (tentativeHandler.getResponse().getHeaders("PROXY-AUTHENTICATE").contains("NTLM")) {
								return ntlmAuthenticate(modifiedRequest, finalRequest, tentativeHandler, processor);
							} else {
								return CompletionHandlerFuture.completeEmpty();
							}
						} else {
							authenticated = true;
							prepareChannel();
							return CompletionHandlerFuture.completeEmpty();
						}
					} else {
						return CompletableFuture.failedFuture(new IOException("Connection closed"));
					}
				});
    }

    private CompletableFuture<Void> ntlmAuthenticate(HttpRequest modifiedRequest, HttpRequest finalRequest, CapturingHandler handler,
            StatefulInputProcessor<HttpHandler, NtlmHttpProcessingContext> processor) {
        Type1Message message1 = new Type1Message(TYPE_1_FLAGS, null, null);
        modifiedRequest.setHeader("PROXY-AUTHORIZATION", NTLM + Base64.encode(message1.toByteArray()));
        return socket.sendHeader(modifiedRequest).thenCompose(x -> readResponse(handler, processor))
        		.thenCompose(result -> {
        			CompletableFuture<Void> retValue = null;
        			if (result >= 0) {
        				try {
        					switch (handler.getResponse().getCode()) {
        					case 407:
        						String authenticate = handler.getResponse().getHeader("PROXY-AUTHENTICATE");
        						if (authenticate != null) {
        							if (authenticate.startsWith(NTLM)) {
                                        String base64 = authenticate.substring(5);
                                        Type2Message message2 = new Type2Message(Base64.decode(base64));
                                        Type3Message message3 = new Type3Message(message2, password, domain, username, null,
                                                message2.getFlags());
                                        finalRequest.setHeader("Proxy-Authorization",
                                                NTLM + Base64.encode(message3.toByteArray()));
                                        return socket.sendHeader(finalRequest).thenAccept(x -> {
                                        	authenticated = true;
                                        	prepareChannel();
                                        });
        							} else {
        								retValue = CompletableFuture.failedFuture(new IOException("Unrecognized proxy authentication protocol: " + authenticate));        								
        							}
        						} else {
        							retValue = CompletionHandlerFuture.completeEmpty();
        						}
        						break;
                            case 200:
                                authenticated = true;
                                prepareChannel();
                                retValue = CompletionHandlerFuture.completeEmpty();
                                break;
                            default:
                                // this happens only in HTTP with disallowed connections.
                                retValue = socket.disconnect();
        					}
                        } catch (IOException e) {
                            retValue = CompletableFuture.failedFuture(e);
                        }
        			} else {
                        retValue = CompletableFuture.failedFuture(new IOException("Connection Closed"));
        			}
        			return retValue;
        		});
    }

    private CompletableFuture<Integer> readResponse(CapturingHandler handler,
            StatefulInputProcessor<HttpHandler, NtlmHttpProcessingContext> processor) {
        handler.reset();
        NtlmHttpProcessingContext context = processingContextFactory.create();
        return readResponse(handler, processor, context);
    }

	private CompletableFuture<Integer> readResponse(CapturingHandler handler,
			StatefulInputProcessor<HttpHandler, NtlmHttpProcessingContext> processor,
			NtlmHttpProcessingContext context) {
        if (!handler.isFinished()) {
            readBuffer.clear();
            return socket.read(readBuffer).thenCompose(x -> {
            	context.addBytesRead(x);
                readBuffer.flip();
                return processor.process(context);
            }).thenCompose(x -> readResponse(handler, processor, context));
        } else {
            return CompletableFuture.completedFuture(context.getBytesRead());
        }
    }
}
