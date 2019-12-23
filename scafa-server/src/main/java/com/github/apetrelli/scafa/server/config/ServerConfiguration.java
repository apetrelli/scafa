package com.github.apetrelli.scafa.server.config;

import java.util.List;

import com.github.apetrelli.scafa.http.proxy.ProxyHttpConnectionFactory;

public interface ServerConfiguration {

    List<String> getExcludes();
    
    ProxyHttpConnectionFactory getProxyHttpConnectionFactory();
}
