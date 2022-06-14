package com.github.apetrelli.scafa.http.client.impl;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.client.HttpClientHandler;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HttpPipelineContext {

    private final HttpRequest request;

    private final HttpClientHandler handler;

    private HttpResponse response;

    public HttpClientHandler getHandler() {
        return handler;
    }

    public HttpRequest getRequest() {
        return request;
    }

    public HttpResponse getResponse() {
        return response;
    }

    public void setResponse(HttpResponse response) {
        this.response = response;
    }
}
