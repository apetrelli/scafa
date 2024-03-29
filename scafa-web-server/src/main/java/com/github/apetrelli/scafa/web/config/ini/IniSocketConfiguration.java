package com.github.apetrelli.scafa.web.config.ini;

import java.util.List;

import org.ini4j.Profile.Section;

import com.github.apetrelli.scafa.web.config.PathConfiguration;
import com.github.apetrelli.scafa.web.config.Protocol;
import com.github.apetrelli.scafa.web.config.SocketConfiguration;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class IniSocketConfiguration implements SocketConfiguration {

	private final Section section;
	
	private List<PathConfiguration> paths;

	@Override
	public int getPort() {
		return section.get("port", Integer.class, 80);
	}

	@Override
	public Protocol getProtocol() {
		String protocol = section.get("protocol");
		return protocol != null ? Protocol.valueOf(protocol) : Protocol.HTTP;
	}

	@Override
	public List<PathConfiguration> getPaths() {
		return paths;
	}
	
	public void setPaths(List<PathConfiguration> paths) {
		this.paths = paths;
	}

}
