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
package com.github.apetrelli.scafa.http.proxy.sync.ntlm;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.github.apetrelli.scafa.http.HttpException;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.impl.HttpStateMachine;
import com.github.apetrelli.scafa.http.proxy.HttpConnectRequest;
import com.github.apetrelli.scafa.http.proxy.HttpRequestManipulator;
import com.github.apetrelli.scafa.http.proxy.ntlm.NtlmHttpProcessingContext;
import com.github.apetrelli.scafa.http.proxy.ntlm.NtlmHttpProcessingContextFactory;
import com.github.apetrelli.scafa.http.proxy.sync.MappedProxyHttpConnectionFactory;
import com.github.apetrelli.scafa.http.proxy.sync.connection.AbstractUpstreamProxyHttpConnection;
import com.github.apetrelli.scafa.http.sync.HttpHandler;
import com.github.apetrelli.scafa.http.sync.HttpSyncSocket;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.processor.ProcessorFactory;
import com.github.apetrelli.scafa.proto.sync.RunnableStarter;
import com.github.apetrelli.scafa.proto.sync.SyncSocket;
import com.github.apetrelli.scafa.proto.sync.processor.DataHandler;
import com.github.apetrelli.scafa.proto.sync.processor.impl.StatefulInputProcessor;

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

    private HttpStateMachine<HttpHandler, Void> stateMachine;

    private TentativeHandler tentativeHandler;

    private NtlmHttpProcessingContextFactory processingContextFactory;

    private ByteBuffer readBuffer = ByteBuffer.allocate(16384);

	public NtlmProxyHttpConnection(MappedProxyHttpConnectionFactory factory,
			ProcessorFactory<DataHandler, SyncSocket> clientProcessorFactory,
			RunnableStarter runnableStarter, HttpSyncSocket<HttpResponse> sourceChannel,
			HttpSyncSocket<HttpRequest> socket, HostPort destinationSocketAddress, String domain, String username,
			String password, HttpStateMachine<HttpHandler, Void> stateMachine, HttpRequestManipulator manipulator) {
		super(factory, clientProcessorFactory, runnableStarter, sourceChannel, socket, destinationSocketAddress,
				manipulator);
        this.domain = domain;
        this.username = username;
        this.password = password;
        this.stateMachine = stateMachine;
        tentativeHandler = new TentativeHandler(sourceChannel);
        processingContextFactory = new NtlmHttpProcessingContextFactory();
    }

    @Override
    protected void doSendHeader(HttpRequest request) {
        if (!authenticated) {
            authenticate(request);
        } else {
            socket.sendHeader(request);
        }
    }
    @Override
    protected void doConnect(HttpConnectRequest request) {
        if (!authenticated) {
            authenticateOnConnect(request);
        } else {
            socket.sendHeader(request);
        }
    }

    private void authenticateOnConnect(HttpRequest request) {
        HttpRequest modifiedRequest = new HttpRequest(request);
        modifiedRequest.setHeader("Proxy-Connection", "keep-alive");
        StatefulInputProcessor<HttpHandler, NtlmHttpProcessingContext> processor = new StatefulInputProcessor<HttpHandler, NtlmHttpProcessingContext>(tentativeHandler, stateMachine);
        ntlmAuthenticate(modifiedRequest, modifiedRequest, tentativeHandler, processor);
    }

    private void authenticate(HttpRequest request) {
        HttpRequest finalRequest = new HttpRequest(request);
        finalRequest.setHeader("Proxy-Connection", "keep-alive");
        HttpRequest modifiedRequest = new HttpRequest(finalRequest);
        String length = request.getHeader("CONTENT-LENGTH");
        if (length != null) {
            modifiedRequest.setHeader("Content-Length", "0");
        }
        StatefulInputProcessor<HttpHandler, NtlmHttpProcessingContext> processor = new StatefulInputProcessor<>(
                tentativeHandler, stateMachine);
        socket.sendHeader(modifiedRequest);
        int x = readResponse(tentativeHandler, processor);
		if (x >= 0) {
			if (tentativeHandler.isNeedsAuthorizing()) {
				tentativeHandler.setOnlyCaptureMode(true);
				if (tentativeHandler.getResponse().getHeaders("PROXY-AUTHENTICATE").contains("NTLM")) {
					ntlmAuthenticate(modifiedRequest, finalRequest, tentativeHandler, processor);
				}
			} else {
				authenticated = true;
				prepareChannel();
			}
		} else {
			throw new HttpException("Connection closed");
		}
    }

    private void ntlmAuthenticate(HttpRequest modifiedRequest, HttpRequest finalRequest, CapturingHandler handler,
            StatefulInputProcessor<HttpHandler, NtlmHttpProcessingContext> processor) {
        Type1Message message1 = new Type1Message(TYPE_1_FLAGS, null, null);
        modifiedRequest.setHeader("PROXY-AUTHORIZATION", NTLM + Base64.encode(message1.toByteArray()));
        socket.sendHeader(modifiedRequest);
        int result = readResponse(handler, processor);
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
                            socket.sendHeader(finalRequest);
                        	authenticated = true;
                        	prepareChannel();
						} else {
							throw new HttpException("Unrecognized proxy authentication protocol: " + authenticate);        								
						}
					}
					break;
                case 200:
                    authenticated = true;
                    prepareChannel();
                    break;
                default:
                    // this happens only in HTTP with disallowed connections.
                    socket.disconnect();
				}
            } catch (IOException e) {
            	throw new HttpException(e);
            }
		} else {
            throw new HttpException("Connection Closed");
		}
    }

    private int readResponse(CapturingHandler handler,
            StatefulInputProcessor<HttpHandler, NtlmHttpProcessingContext> processor) {
        handler.reset();
        NtlmHttpProcessingContext context = processingContextFactory.create();
        return readResponse(handler, processor, context);
    }

	private int readResponse(CapturingHandler handler,
			StatefulInputProcessor<HttpHandler, NtlmHttpProcessingContext> processor,
			NtlmHttpProcessingContext context) {
        while (!handler.isFinished()) {
            readBuffer.clear();
            int x = socket.read(readBuffer);
            if (x >= 0) {
	            context.addBytesRead(x);
	            readBuffer.flip();
	            processor.process(context);
            } else {
            	throw new HttpException("Connection closed");
            }
        }
        return context.getBytesRead();
    }
}
