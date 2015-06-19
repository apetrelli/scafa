package com.github.apetrelli.scafa.http.ntlm;

import java.io.IOException;
import java.net.InetSocketAddress;
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

import org.ini4j.Profile.Section;

import com.github.apetrelli.scafa.http.HttpByteSink;
import com.github.apetrelli.scafa.http.HttpConnectionFactory;
import com.github.apetrelli.scafa.http.HttpHandler;
import com.github.apetrelli.scafa.http.HttpInput;
import com.github.apetrelli.scafa.http.HttpStatus;
import com.github.apetrelli.scafa.http.impl.AbstractHttpConnection;
import com.github.apetrelli.scafa.http.impl.DefaultHttpByteSink;
import com.github.apetrelli.scafa.http.impl.HostPort;
import com.github.apetrelli.scafa.processor.BufferProcessor;
import com.github.apetrelli.scafa.processor.impl.DefaultBufferProcessor;
import com.github.apetrelli.scafa.server.Status;

public class NtlmProxyHttpConnection extends AbstractHttpConnection {
    
    private static final int TYPE_1_FLAGS = NtlmFlags.NTLMSSP_NEGOTIATE_128 | NtlmFlags.NTLMSSP_NEGOTIATE_ALWAYS_SIGN
            | NtlmFlags.NTLMSSP_NEGOTIATE_LM_KEY | NtlmFlags.NTLMSSP_NEGOTIATE_TARGET_INFO
            | NtlmFlags.NTLMSSP_NEGOTIATE_OEM | NtlmFlags.NTLMSSP_NEGOTIATE_UNICODE;
    
    private boolean authenticated = false;
    
    private HttpConnectionFactory factory;
    
    private HostPort socketAddress;
    
    private String domain, username, password;
    
    public NtlmProxyHttpConnection(HttpConnectionFactory factory, AsynchronousSocketChannel sourceChannel,
            Section config) throws IOException {
        super(factory, sourceChannel);
        this.factory = factory;
        this.socketAddress = new HostPort(config.get("host"), config.get("port", Integer.class));
        domain = config.get("domain");
        username = config.get("username");
        password = config.get("password");
        getFuture(channel.connect(new InetSocketAddress(socketAddress.getHost(), socketAddress.getPort())));
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
                        Type3Message message3 = new Type3Message(message2, password, domain, username,
                                null, message2.getFlags());
                        finalHeaders.put("PROXY-AUTHORIZATION", Arrays.asList("NTLM " + Base64.encode(message3.toByteArray())));
                        sendHeader(requestLine, finalHeaders);
                        authenticated = true;
                        prepareChannel(factory, sourceChannel, socketAddress);
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
