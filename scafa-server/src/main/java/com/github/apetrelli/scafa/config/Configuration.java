package com.github.apetrelli.scafa.config;

import java.util.List;

public interface Configuration {

    int getPort();
    
    List<ServerConfiguration> getServerConfigurations();
    
    ServerConfiguration getServerConfigurationByHost(String host);
}
