package com.github.apetrelli.scafa.web.config;

public interface GatewayPathConfiguration extends PathConfiguration {

	String getDestinationHost();
	
	int getDestinationPort();
}
