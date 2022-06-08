package com.github.apetrelli.scafa.proxy.config;

import java.util.List;

public interface Configuration<T> {

    int getPort();
    
    List<ServerConfiguration<T>> getServerConfigurations();
    
    ServerConfiguration<T> getServerConfigurationByHost(String host);
}
