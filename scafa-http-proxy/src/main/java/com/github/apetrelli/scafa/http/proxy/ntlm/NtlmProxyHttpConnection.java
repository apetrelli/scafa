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

import static com.github.apetrelli.scafa.http.HttpHeaders.CONTENT_LENGTH;
import static com.github.apetrelli.scafa.http.HttpHeaders.CONTENT_LENGTH_0;
import static com.github.apetrelli.scafa.http.HttpHeaders.KEEP_ALIVE;
import static com.github.apetrelli.scafa.http.HttpHeaders.PROXY_AUTHENTICATE;
import static com.github.apetrelli.scafa.http.HttpHeaders.PROXY_AUTHENTICATE_NTLM;
import static com.github.apetrelli.scafa.http.HttpHeaders.PROXY_AUTHORIZATION;
import static com.github.apetrelli.scafa.http.HttpHeaders.PROXY_CONNECTION;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.http.HttpHandler;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.gateway.MappedGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.http.impl.HttpStateMachine;
import com.github.apetrelli.scafa.http.proxy.HttpConnectRequest;
import com.github.apetrelli.scafa.http.proxy.HttpRequestManipulator;
import com.github.apetrelli.scafa.http.proxy.impl.AbstractUpstreamProxyHttpConnection;
import com.github.apetrelli.scafa.proto.async.AsyncSocket;
import com.github.apetrelli.scafa.proto.async.processor.DataHandler;
import com.github.apetrelli.scafa.proto.async.processor.impl.StatefulInputProcessor;
import com.github.apetrelli.scafa.proto.async.util.CompletionHandlerFuture;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.processor.ProcessorFactory;
import com.github.apetrelli.scafa.proto.util.AsciiString;

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

    private HttpStateMachine<HttpHandler, CompletableFuture<Void>> stateMachine;

    private TentativeHandler tentativeHandler;

    private NtlmHttpProcessingContextFactory processingContextFactory;

    private ByteBuffer readBuffer = ByteBuffer.allocate(16384);

	public NtlmProxyHttpConnection(MappedGatewayHttpConnectionFactory<?> factory,
			ProcessorFactory<DataHandler, AsyncSocket> clientProcessorFactory, HttpAsyncSocket<HttpResponse> sourceChannel,
			HttpAsyncSocket<HttpRequest> socket, HostPort destinationSocketAddress, String domain, String username,
			String password, HttpStateMachine<HttpHandler, CompletableFuture<Void>> stateMachine, HttpRequestManipulator manipulator) {
        super(factory, clientProcessorFactory, sourceChannel, socket, destinationSocketAddress, manipulator);
        this.domain = domain;
        this.username = username;
        this.password = password;
        this.stateMachine = stateMachine;
        tentativeHandler = new TentativeHandler(sourceChannel);
        processingContextFactory = new NtlmHttpProcessingContextFactory();
    }

    @Override
    protected CompletableFuture<Void> doSendHeader(HttpRequest request, ByteBuffer writeBuffer) {
        if (!authenticated) {
            return authenticate(request, writeBuffer);
        } else {
            return socket.sendHeader(request, writeBuffer);
        }
    }
    @Override
    protected CompletableFuture<Void> doConnect(HttpConnectRequest request, ByteBuffer writeBuffer) {
        if (!authenticated) {
            return authenticateOnConnect(request, writeBuffer);
        } else {
            return socket.sendHeader(request, writeBuffer);
        }
    }

    private CompletableFuture<Void> authenticateOnConnect(HttpRequest request, ByteBuffer writeBuffer) {
        HttpRequest modifiedRequest = new HttpRequest(request);
        modifiedRequest.setHeader(PROXY_CONNECTION, KEEP_ALIVE);
        StatefulInputProcessor<HttpHandler, NtlmHttpProcessingContext> processor = new StatefulInputProcessor<>(tentativeHandler, stateMachine);
        return ntlmAuthenticate(modifiedRequest, modifiedRequest, tentativeHandler, processor, writeBuffer);
    }

    private CompletableFuture<Void> authenticate(HttpRequest request, ByteBuffer writeBuffer) {
        HttpRequest finalRequest = new HttpRequest(request);
        finalRequest.setHeader(PROXY_CONNECTION, KEEP_ALIVE);
        HttpRequest modifiedRequest = new HttpRequest(finalRequest);
        AsciiString length = request.getHeader(CONTENT_LENGTH);
        if (length != null) {
            modifiedRequest.setHeader(CONTENT_LENGTH, CONTENT_LENGTH_0);
        }
        StatefulInputProcessor<HttpHandler, NtlmHttpProcessingContext> processor = new StatefulInputProcessor<>(
                tentativeHandler, stateMachine);
		return socket.sendHeader(modifiedRequest, writeBuffer).thenCompose(x -> readResponse(tentativeHandler, processor, writeBuffer))
				.thenCompose(x -> {
					if (x >= 0) {
						if (tentativeHandler.isNeedsAuthorizing()) {
							tentativeHandler.setOnlyCaptureMode(true);
							if (tentativeHandler.getResponse().getHeaders(PROXY_AUTHENTICATE).contains(PROXY_AUTHENTICATE_NTLM)) {
								return ntlmAuthenticate(modifiedRequest, finalRequest, tentativeHandler, processor, writeBuffer);
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

    private CompletableFuture<Void> ntlmAuthenticate(HttpRequest modifiedRequest, HttpRequest finalRequest, TentativeHandler handler,
            StatefulInputProcessor<HttpHandler, NtlmHttpProcessingContext> processor, ByteBuffer writeBuffer) {
        Type1Message message1 = new Type1Message(TYPE_1_FLAGS, null, null);
        modifiedRequest.setHeader(PROXY_AUTHORIZATION, new AsciiString(NTLM + Base64.encode(message1.toByteArray())));
        return socket.sendHeader(modifiedRequest, writeBuffer).thenCompose(x -> readResponse(handler, processor, writeBuffer))
        		.thenCompose(result -> {
        			CompletableFuture<Void> retValue = null;
        			if (result >= 0) {
        				try {
        					switch (handler.getResponse().getCode().toString()) {
        					case "407":
        						AsciiString authenticate = handler.getResponse().getHeader(PROXY_AUTHENTICATE);
        						if (authenticate != null) {
        							String authenticateString = authenticate.toString();
        							if (authenticateString.startsWith(NTLM)) {
                                        String base64 = authenticateString.substring(5);
                                        Type2Message message2 = new Type2Message(Base64.decode(base64));
                                        Type3Message message3 = new Type3Message(message2, password, domain, username, null,
                                                message2.getFlags());
                                        finalRequest.setHeader(PROXY_AUTHORIZATION,
                                                new AsciiString(NTLM + Base64.encode(message3.toByteArray())));
                                        return socket.sendHeader(finalRequest, writeBuffer).thenAccept(x -> {
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
                            case "200":
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
        		}).whenComplete((x, y) -> handler.reset());
    }

    private CompletableFuture<Integer> readResponse(TentativeHandler handler,
            StatefulInputProcessor<HttpHandler, NtlmHttpProcessingContext> processor, ByteBuffer writeBuffer) {
        handler.reset();
        handler.setWriteBuffer(writeBuffer);
        NtlmHttpProcessingContext context = processingContextFactory.create();
        return readResponse(handler, processor, context);
    }

	private CompletableFuture<Integer> readResponse(CapturingHandler handler,
			StatefulInputProcessor<HttpHandler, NtlmHttpProcessingContext> processor,
			NtlmHttpProcessingContext context) {
        if (!handler.isFinished()) {
            readBuffer.clear();
            return socket.read(readBuffer).thenCompose(x -> {
            	if (x >= 0) {
					context.addBytesRead(x);
					readBuffer.flip();
					return processor.process(context).thenCompose(y -> readResponse(handler, processor, context));
            	} else {
            		return CompletableFuture.failedFuture(new IOException("Connection Closed"));
            	}
            });
        } else {
            return CompletableFuture.completedFuture(context.getBytesRead());
        }
    }
}
