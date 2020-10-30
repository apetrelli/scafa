package com.github.apetrelli.scafa.server.config;

import java.util.List;

public interface ServerConfiguration<T> {

    List<String> getExcludes();
    
    T getProxyHttpConnectionFactory();
}
