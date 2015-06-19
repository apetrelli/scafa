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
        String requestLine = method + " " + url + " " + httpVersion;
        if (!authenticated) {
            authenticate(requestLine, headers);
        } else{
            sendHeader(requestLine, headers);
        }
    }
    
    @Override
    public void connect(String method, String host, int port, String httpVersion, Map<String, List<String>> headers)
            throws IOException {
        String requestLine = method + " " + host + ":" + port + " " + httpVersion;
        if (!authenticated) {
            authenticateOnConnect(headers, requestLine);
        } else {
            sendHeader(requestLine, headers);
        }
    }

    private void authenticateOnConnect(Map<String, List<String>> headers, String requestLine) throws IOException {
        Map<String, List<String>> modifiedHeaders = new LinkedHashMap<>(headers);
        modifiedHeaders.put("PROXY-CONNECTION", Arrays.asList("keep-alive"));
        CapturingHandler handler = new CapturingHandler();
        HttpByteSink sink = new DefaultHttpByteSink<HttpHandler>(handler);
        BufferProcessor<HttpInput, HttpByteSink> processor = new DefaultBufferProcessor<>(sink);
        ntlmAuthenticate(requestLine, modifiedHeaders, modifiedHeaders, handler, sink, processor);
    }

    private void authenticate(String requestLine, Map<String, List<String>> headers) throws IOException {
        Map<String, List<String>> finalHeaders = new LinkedHashMap<>(headers);
        finalHeaders.put("PROXY-CONNECTION", Arrays.asList("keep-alive"));
        Map<String, List<String>> modifiedHeaders = finalHeaders;
        List<String> lengthList = headers.get("CONTENT-LENGTH");
        if (lengthList != null && !lengthList.isEmpty()) {
            modifiedHeaders.put("CONTENT-LENGTH", Arrays.asList("0"));
        }
        if (sendHeader(requestLine, modifiedHeaders) >= 0) {
            CapturingHandler handler = new CapturingHandler();
            HttpByteSink sink = new DefaultHttpByteSink<HttpHandler>(handler);
            BufferProcessor<HttpInput, HttpByteSink> processor = new DefaultBufferProcessor<>(sink);
            if (readResponse(handler, sink, processor) >= 0 && handler.getResponseCode() == 407
                    && handler.getHeaders().get("PROXY-AUTHENTICATE").contains("NTLM")) {
                ntlmAuthenticate(requestLine, modifiedHeaders, finalHeaders, handler, sink, processor);
            }
        }
    }

    private void ntlmAuthenticate(String requestLine, Map<String, List<String>> modifiedHeaders,
            Map<String, List<String>> finalHeaders, CapturingHandler handler, HttpByteSink sink,
            BufferProcessor<HttpInput, HttpByteSink> processor) throws IOException {
        Type1Message message1 = new Type1Message(TYPE_1_FLAGS, null, null);
        modifiedHeaders.put("PROXY-AUTHORIZATION", Arrays.asList("NTLM " + Base64.encode(message1.toByteArray())));
        if (sendHeader(requestLine, modifiedHeaders) >= 0) {
            if (readResponse(handler, sink, processor) >= 0 && handler.getResponseCode() == 407) {
                List<String> authenticates = handler.getHeaders().get("PROXY-AUTHENTICATE");
                if (authenticates != null && authenticates.size() == 1) {
                    String authenticate = authenticates.get(0);
                    if (authenticate.startsWith("NTLM ")) {
                        String base64 = authenticate.substring(5);
                        Type2Message message2 = new Type2Message(Base64.decode(base64));
                        // TODO use configuration.
                        Type3Message message3 = new Type3Message(message2, "password", "domain", "username",
                                null, message2.getFlags());
                        finalHeaders.put("PROXY-AUTHORIZATION", Arrays.asList("NTLM " + Base64.encode(message3.toByteArray())));
                        sendHeader(requestLine, finalHeaders);
                        authenticated = true;
                        super.prepareChannel(factory, sourceChannel, socketAddress);
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
