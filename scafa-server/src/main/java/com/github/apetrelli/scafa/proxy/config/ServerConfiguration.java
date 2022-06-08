package com.github.apetrelli.scafa.proxy.config;

import java.util.List;

public interface ServerConfiguration<T> {

    List<String> getExcludes();
    
    T getProxyHttpConnectionFactory();
}
