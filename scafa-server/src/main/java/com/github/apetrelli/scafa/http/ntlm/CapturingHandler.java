package com.github.apetrelli.scafa.http.ntlm;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.github.apetrelli.scafa.http.impl.HttpHandlerSupport;

public class CapturingHandler extends HttpHandlerSupport {
    
    private boolean finished = false;
    
    private String httpVersion;
    
    private int responseCode;
    
    private String responseMessage;
    
    private Map<String, List<String>> headers;
    
    @Override
    public void onResponseHeader(String httpVersion, int responseCode, String responseMessage,
            Map<String, List<String>> headers) throws IOException {
        this.httpVersion = httpVersion;
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.headers = headers;
    }
    
    @Override
    public void onEnd() throws IOException {
        finished = true;
    }
    
    public boolean isFinished() {
        return finished;
    }
    
    public void reset() {
        finished = false;
        responseCode = 0;
        responseMessage = null;
        headers = null;
    }
    
    public String getHttpVersion() {
        return httpVersion;
    }
    
    public int getResponseCode() {
        return responseCode;
    }
    
    public String getResponseMessage() {
        return responseMessage;
    }
    
    public Map<String, List<String>> getHeaders() {
        return headers;
    }
}