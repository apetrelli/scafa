package com.github.apetrelli.scafa.web.config;

import java.util.List;
import java.util.Map;

public interface Configuration {

	Map<String, String> getMimeTypeConfig();
	
	List<SocketConfiguration> getSocketConfigurations();
}
