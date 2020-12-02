package com.github.apetrelli.scafa.web.config;

import java.util.List;
import java.util.Map;

import com.github.apetrelli.scafa.proto.util.AsciiString;

public interface Configuration {

	Map<String, AsciiString> getMimeTypeConfig();
	
	List<SocketConfiguration> getSocketConfigurations();
}
