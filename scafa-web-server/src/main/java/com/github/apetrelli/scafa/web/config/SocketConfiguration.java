package com.github.apetrelli.scafa.web.config;

import java.util.List;

public interface SocketConfiguration {

	int getPort();
	
	Protocol getProtocol();
	
	List<PathConfiguration> getPaths();
}
