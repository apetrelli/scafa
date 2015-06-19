package com.github.apetrelli.scafa.server.processor.http.ntlm;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jcifs.ntlmssp.NtlmFlags;
import jcifs.ntlmssp.Type1Message;
import jcifs.ntlmssp.Type2Message;
import jcifs.ntlmssp.Type3Message;
import jcifs.util.Base64;

import com.github.apetrelli.scafa.server.Status;
import com.github.apetrelli.scafa.server.processor.BufferProcessor;
import com.github.apetrelli.scafa.server.processor.http.HttpByteSink;
import com.github.apetrelli.scafa.server.processor.http.HttpConnectionFactory;
import com.github.apetrelli.scafa.server.processor.http.HttpHandler;
import com.github.apetrelli.scafa.server.processor.http.HttpInput;
import com.github.apetrelli.scafa.server.processor.http.HttpStatus;
import com.github.apetrelli.scafa.server.processor.http.impl.DefaultHttpByteSink;
import com.github.apetrelli.scafa.server.processor.http.impl.DirectHttpConnection;
import com.github.apetrelli.scafa.server.processor.impl.DefaultBufferProcessor;

public class NtlmProxyHttpConnection extends DirectHttpConnection {
    
    private static final int TYPE_1_FLAGS = NtlmFlags.NTLMSSP_NEGOTIATE_128 | NtlmFlags.NTLMSSP_NEGOTIATE_ALWAYS_SIGN
            | NtlmFlags.NTLMSSP_NEGOTIATE_LM_KEY | NtlmFlags.NTLMSSP_NEGOTIATE_TARGET_INFO
            | NtlmFlags.NTLMSSP_NEGOTIATE_OEM | NtlmFlags.NTLMSSP_NEGOTIATE_UNICODE;
    
    private boolean authenticated = false;
    
    private HttpConnectionFactory factory;
    
    private SocketAddress socketAddress;
    
    public NtlmProxyHttpConnection(HttpConnectionFactory factory, AsynchronousSocketChannel sourceChannel,
            SocketAddress socketAddress) throws IOException {
        super(factory, sourceChannel, socketAddress);
        this.factory = factory;
        this.socketAddress = socketAddress;
    }

    @Override
    protected void prepareChannel(HttpConnectionFactory factory, AsynchronousSocketChannel sourceChannel,
            SocketAddress socketAddress) throws IOException {
        // Do nothing
    }
    
    @Override
    public void sendHeader(String method, String url, String httpVersion, Map<String, List<String>> headers)
            throws IOException {
        if (!authenticated) {
            authenticate(method + " " + url + " " + httpVersion, headers);
        } else{
            sendHeader(method + " " + url + " " + httpVersion, headers);
        }
    }
    
    @Override
    public void connect(String method, String host, int port, String httpVersion, Map<String, List<String>> headers)
            throws IOException {
        if (!authenticated) {
            authenticate(method + " " + host + ":" + port + " " + httpVersion, headers);
        } else {
            super.connect(method, host, port, httpVersion, headers);
        }
    }

    private void authenticate(String requestLine, Map<String, List<String>> headers) throws IOException {
        Map<String, List<String>> modifiedHeaders = new LinkedHashMap<>(headers);
        modifiedHeaders.put("PROXY-CONNECTION", Arrays.asList("keep-alive"));
        if (super.sendHeader(requestLine, modifiedHeaders) >= 0) {
            CapturingHandler handler = new CapturingHandler();
            HttpByteSink sink = new DefaultHttpByteSink<HttpHandler>(handler);
            BufferProcessor<HttpInput, HttpByteSink> processor = new DefaultBufferProcessor<>(sink);
            if (readResponse(handler, sink, processor) >= 0 && handler.getResponseCode() == 407
                    && handler.getHeaders().get("PROXY-AUTHENTICATE").contains("NTLM")) {
                Type1Message message1 = new Type1Message(TYPE_1_FLAGS, null, null);
                modifiedHeaders.put("PROXY-AUTHORIZATION", Arrays.asList("NTLM " + Base64.encode(message1.toByteArray())));
                if (super.sendHeader(requestLine, modifiedHeaders) >= 0) {
                    if (readResponse(handler, sink, processor) >= 0 && handler.getResponseCode() == 407) {
                        List<String> authenticates = handler.getHeaders().get("PROXY-AUTHENTICATE");
                        if (authenticates != null && authenticates.size() == 1) {
                            String authenticate = authenticates.get(0);
                            if (authenticate.startsWith("NTLM ")) {
                                String base64 = authenticate.substring(5);
                                Type2Message message2 = new Type2Message(Base64.decode(base64));
                                // TODO Get from configuration.
                                Type3Message message3 = new Type3Message(message2, "password", "domain", "username",
                                        null, message2.getFlags());
                                modifiedHeaders.put("PROXY-AUTHORIZATION", Arrays.asList("NTLM " + Base64.encode(message3.toByteArray())));
                                sendHeader(requestLine, modifiedHeaders);
                                authenticated = true;
                                super.prepareChannel(factory, sourceChannel, socketAddress);
                            }
                        }
                    }
                }
            }
        }
    }

    private Integer readResponse(CapturingHandler handler, HttpByteSink sink,
            BufferProcessor<HttpInput, HttpByteSink> processor) throws IOException {
        handler.reset();
        Integer retValue = null;
        sink.reset();
        HttpInput input = sink.createInput();
        input.setBuffer(readBuffer);
        Status<HttpInput, HttpByteSink> status = HttpStatus.IDLE;
        while (!handler.isFinished()) {
            readBuffer.clear();
            retValue = getFuture(channel.read(readBuffer));
            readBuffer.flip();
            status = processor.process(input, status);
        }
        readBuffer.clear();
        return retValue;
    }
}
