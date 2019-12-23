package com.github.apetrelli.scafa.config;

import java.util.List;

import com.github.apetrelli.scafa.http.proxy.ProxyHttpConnectionFactory;

public interface ServerConfiguration {

    String getName();
    
    ServerType getType();
    
    String getInterfaceName();
    
    boolean isForceIPV4();
    
    String getHost();
    
    int getPort();
    
    String getDomain();
    
    String getUsername();
    
    String getPassword();
    
    String getManipulatorClassName();
    
    List<String> getExcludes();
    
    ProxyHttpConnectionFactory getProxyHttpConnectionFactory();
}
