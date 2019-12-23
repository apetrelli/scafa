package com.github.apetrelli.scafa.server.config;

import java.util.List;

public interface Configuration {

    int getPort();
    
    List<ServerConfiguration> getServerConfigurations();
    
    ServerConfiguration getServerConfigurationByHost(String host);
}
