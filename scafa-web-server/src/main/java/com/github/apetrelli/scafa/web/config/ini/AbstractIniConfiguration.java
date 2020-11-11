package com.github.apetrelli.scafa.web.config.ini;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;

import com.github.apetrelli.scafa.web.config.Configuration;
import com.github.apetrelli.scafa.web.config.PathConfiguration;
import com.github.apetrelli.scafa.web.config.SocketConfiguration;

public class AbstractIniConfiguration implements Configuration{
	
	private List<SocketConfiguration> sockets;
	
	public AbstractIniConfiguration(Ini ini) {
		Map<String, AbstractIniSocketConfiguration> name2socket = new LinkedHashMap<>();
		Map<String, List<PathConfiguration>> socket2path = new LinkedHashMap<>();
		ini.keySet().stream().map(ini::get).forEach(x -> {
			if (x.getName().startsWith("socket-")) {
				name2socket.put(x.getName().substring("socket-".length()), new AbstractIniSocketConfiguration(x));
			} else if (x.getName().startsWith("static-")) {
				AbstractIniStaticPathConfiguration path = new AbstractIniStaticPathConfiguration(x);
				mapToSocket(socket2path, x, path);
			} else if (x.getName().startsWith("gateway-")) {
				AbstractIniGatewayPathConfiguration path = new AbstractIniGatewayPathConfiguration(x);
				mapToSocket(socket2path, x, path);
			}
		});
		name2socket.entrySet().forEach(x -> x.getValue().setPaths(new ArrayList<>(socket2path.get(x.getKey()))));
		sockets = new ArrayList<>(name2socket.values());
	}
	
	@Override
	public List<SocketConfiguration> getSocketConfigurations() {
		return sockets;
	}

	private void mapToSocket(Map<String, List<PathConfiguration>> socket2path, Section section,
			PathConfiguration path) {
		String[] socketNames = section.get("sockets").split("\\s*,\\s*");
		for (String socketName : socketNames) {
			List<PathConfiguration> paths = socket2path.computeIfAbsent(socketName, x -> new ArrayList<>());
			paths.add(path);
		}
	}

}
