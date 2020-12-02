package com.github.apetrelli.scafa.web.config.ini;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;

import com.github.apetrelli.scafa.proto.util.AsciiString;
import com.github.apetrelli.scafa.web.config.Configuration;
import com.github.apetrelli.scafa.web.config.PathConfiguration;
import com.github.apetrelli.scafa.web.config.SocketConfiguration;

public class IniConfiguration implements Configuration{
	
	private Map<String, AsciiString> mimeTypeConfig;
	
	private List<SocketConfiguration> sockets;
	
	public IniConfiguration(Ini ini, String rootFilesystemPath, URL mimeTypeConfigResource) throws IOException {
		prepareMimeTypes(mimeTypeConfigResource);
		prepareSockets(ini, rootFilesystemPath);
	}
	
	@Override
	public Map<String, AsciiString> getMimeTypeConfig() {
		return mimeTypeConfig;
	}
	
	@Override
	public List<SocketConfiguration> getSocketConfigurations() {
		return sockets;
	}

	private void prepareMimeTypes(URL mimeTypeConfigResource) throws IOException {
		mimeTypeConfig = new HashMap<>();
		try (InputStream is = mimeTypeConfigResource.openStream();
				InputStreamReader isr = new InputStreamReader(is, StandardCharsets.US_ASCII);
				BufferedReader br = new BufferedReader(isr)) {
			String line;
			while ((line = br.readLine()) != null) {
				if (!line.startsWith("#")) {
					String[] pieces = line.split("\\s+");
					if (pieces.length >= 2) {
						String mimeType = pieces[0];
						for (int i = 1; i < pieces.length; i++) {
							mimeTypeConfig.put(pieces[i], new AsciiString(mimeType));
						}
					}
				}
			}
		}
	}

	private void prepareSockets(Ini ini, String rootFilesystemPath) {
		Map<String, IniSocketConfiguration> name2socket = new LinkedHashMap<>();
		Map<String, List<PathConfiguration>> socket2path = new LinkedHashMap<>();
		ini.keySet().stream().map(ini::get).forEach(x -> {
			if (x.getName().startsWith("socket-")) {
				name2socket.put(x.getName().substring("socket-".length()), new IniSocketConfiguration(x));
			} else if (x.getName().startsWith("static-")) {
				IniStaticPathConfiguration path = new IniStaticPathConfiguration(x, rootFilesystemPath);
				mapToSocket(socket2path, x, path);
			} else if (x.getName().startsWith("gateway-")) {
				IniGatewayPathConfiguration path = new IniGatewayPathConfiguration(x);
				mapToSocket(socket2path, x, path);
			}
		});
		name2socket.entrySet().forEach(x -> x.getValue().setPaths(new ArrayList<>(socket2path.get(x.getKey()))));
		sockets = new ArrayList<>(name2socket.values());
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
