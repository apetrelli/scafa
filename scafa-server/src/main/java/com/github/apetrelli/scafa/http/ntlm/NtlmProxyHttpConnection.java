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
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ini4j.Profile.Section;

import com.github.apetrelli.scafa.http.HttpByteSink;
import com.github.apetrelli.scafa.http.HttpHandler;
import com.github.apetrelli.scafa.http.HttpInput;
import com.github.apetrelli.scafa.http.HttpStatus;
import com.github.apetrelli.scafa.http.impl.DefaultHttpByteSink;
import com.github.apetrelli.scafa.http.impl.HostPort;
import com.github.apetrelli.scafa.http.proxy.HttpConnectionFactory;
import com.github.apetrelli.scafa.http.proxy.impl.AbstractHttpConnection;
import com.github.apetrelli.scafa.processor.BufferProcessor;
import com.github.apetrelli.scafa.processor.impl.ClientBufferProcessor;
import com.github.apetrelli.scafa.server.Status;
import com.github.apetrelli.scafa.util.HttpUtils;

import jcifs.ntlmssp.NtlmFlags;
import jcifs.ntlmssp.Type1Message;
import jcifs.ntlmssp.Type2Message;
import jcifs.ntlmssp.Type3Message;
import jcifs.util.Base64;

public class NtlmProxyHttpConnection extends AbstractHttpConnection {

    private static final int TYPE_1_FLAGS = NtlmFlags.NTLMSSP_NEGOTIATE_128 | NtlmFlags.NTLMSSP_NEGOTIATE_ALWAYS_SIGN
            | NtlmFlags.NTLMSSP_NEGOTIATE_LM_KEY | NtlmFlags.NTLMSSP_NEGOTIATE_TARGET_INFO
            | NtlmFlags.NTLMSSP_NEGOTIATE_OEM | NtlmFlags.NTLMSSP_NEGOTIATE_UNICODE;

    private static final Logger LOG = Logger.getLogger(NtlmProxyHttpConnection.class.getName());

    private boolean authenticated = false;

    private HttpConnectionFactory factory;

    private HostPort socketAddress;

    private String domain, username, password;

    private TentativeHandler tentativeHandler;

    public NtlmProxyHttpConnection(HttpConnectionFactory factory, AsynchronousSocketChannel sourceChannel,
            Section config) throws IOException {
        super(factory, sourceChannel);
        this.factory = factory;
        this.socketAddress = new HostPort(config.get("host"), config.get("port", Integer.class));
        domain = config.get("domain");
        username = config.get("username");
        password = config.get("password");
        LOG.finest("Trying to connect to " + socketAddress.toString());
        HttpUtils.getFuture(channel.connect(new InetSocketAddress(socketAddress.getHost(), socketAddress.getPort())));
        if (LOG.isLoggable(Level.INFO)) {
            LOG.log(Level.INFO, "Connected thread {0} to port {1}",
                    new Object[] { Thread.currentThread().getName(), channel.getLocalAddress().toString() });
        }
        tentativeHandler = new TentativeHandler(sourceChannel);
    }

    @Override
    public void sendHeader(String method, String url, String httpVersion, Map<String, List<String>> headers)
            throws IOException {
        if (LOG.isLoggable(Level.INFO)) {
            LOG.log(Level.INFO, "Connected thread {0} to port {1} and URL {2}",
                    new Object[] { Thread.currentThread().getName(), channel.getLocalAddress().toString(), url });
        }
        String requestLine = method + " " + url + " " + httpVersion;
        if (!authenticated) {
            authenticate(requestLine, headers);
        } else {
            HttpUtils.sendHeader(requestLine, headers, buffer, channel);
        }
    }

    @Override
    public void connect(String method, String host, int port, String httpVersion, Map<String, List<String>> headers)
            throws IOException {
        if (LOG.isLoggable(Level.INFO)) {
            LOG.log(Level.INFO, "Connected thread {0} to port {1} and host {2}:{3}", new Object[] {
                    Thread.currentThread().getName(), channel.getLocalAddress().toString(), host, port });
        }
        String requestLine = method + " " + host + ":" + port + " " + httpVersion;
        if (!authenticated) {
            authenticateOnConnect(headers, requestLine);
        } else {
            HttpUtils.sendHeader(requestLine, headers, buffer, channel);
        }
    }

    private void authenticateOnConnect(Map<String, List<String>> headers, String requestLine) throws IOException {
        Map<String, List<String>> modifiedHeaders = new LinkedHashMap<>(headers);
        modifiedHeaders.put("PROXY-CONNECTION", Arrays.asList("keep-alive"));
        HttpByteSink sink = new DefaultHttpByteSink<HttpHandler>(tentativeHandler);
        BufferProcessor<HttpInput, HttpByteSink> processor = new ClientBufferProcessor<>(sink);
        ntlmAuthenticate(requestLine, modifiedHeaders, modifiedHeaders, sink, tentativeHandler, processor);
    }

    private void authenticate(String requestLine, Map<String, List<String>> headers) throws IOException {
        Map<String, List<String>> finalHeaders = new LinkedHashMap<>(headers);
        finalHeaders.put("PROXY-CONNECTION", Arrays.asList("keep-alive"));
        Map<String, List<String>> modifiedHeaders = finalHeaders;
        List<String> lengthList = headers.get("CONTENT-LENGTH");
        if (lengthList != null && !lengthList.isEmpty()) {
            modifiedHeaders.put("CONTENT-LENGTH", Arrays.asList("0"));
        }
        if (HttpUtils.sendHeader(requestLine, modifiedHeaders, buffer, channel) >= 0) {
            HttpByteSink sink = new DefaultHttpByteSink<HttpHandler>(tentativeHandler);
            BufferProcessor<HttpInput, HttpByteSink> processor = new ClientBufferProcessor<>(sink);
            if (readResponse(tentativeHandler, sink, processor) >= 0) {
                if (tentativeHandler.isNeedsAuthorizing()) {
                    tentativeHandler.setOnlyCaptureMode(true);
                    if (tentativeHandler.getHeaders().get("PROXY-AUTHENTICATE").contains("NTLM")) {
                        ntlmAuthenticate(requestLine, modifiedHeaders, finalHeaders, sink, tentativeHandler, processor);
                    }
                } else {
                    authenticated = true;
                    prepareChannel(factory, sourceChannel, socketAddress);
                }
            } else {
                throw new IOException("Connection closed");
            }
        }
    }

    private void ntlmAuthenticate(String requestLine, Map<String, List<String>> modifiedHeaders,
            Map<String, List<String>> finalHeaders, HttpByteSink sink, CapturingHandler handler,
            BufferProcessor<HttpInput, HttpByteSink> processor) throws IOException {
        Type1Message message1 = new Type1Message(TYPE_1_FLAGS, null, null);
        modifiedHeaders.put("PROXY-AUTHORIZATION", Arrays.asList("NTLM " + Base64.encode(message1.toByteArray())));
        if (HttpUtils.sendHeader(requestLine, modifiedHeaders, buffer, channel) >= 0) {
            if (readResponse(handler, sink, processor) >= 0) {
                switch (handler.getResponseCode()) {
                case 407:
                    List<String> authenticates = handler.getHeaders().get("PROXY-AUTHENTICATE");
                    if (authenticates != null && authenticates.size() == 1) {
                        String authenticate = authenticates.get(0);
                        if (authenticate.startsWith("NTLM ")) {
                            String base64 = authenticate.substring(5);
                            Type2Message message2 = new Type2Message(Base64.decode(base64));
                            Type3Message message3 = new Type3Message(message2, password, domain, username, null,
                                    message2.getFlags());
                            finalHeaders.put("PROXY-AUTHORIZATION",
                                    Arrays.asList("NTLM " + Base64.encode(message3.toByteArray())));
                            HttpUtils.sendHeader(requestLine, finalHeaders, buffer, channel);
                            authenticated = true;
                            prepareChannel(factory, sourceChannel, socketAddress);
                        }
                    }
                    break;
                case 200:
                    authenticated = true;
                    prepareChannel(factory, sourceChannel, socketAddress);
                    break;
                default:
                    channel.close(); // this happens only in HTTP with unallowed
                                     // connections.
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
