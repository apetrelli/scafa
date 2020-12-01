package com.github.apetrelli.scafa.web.config.ini;

import org.ini4j.Profile.Section;

import com.github.apetrelli.scafa.web.config.GatewayPathConfiguration;

public class IniGatewayPathConfiguration extends IniPathConfiguration implements GatewayPathConfiguration {

	public IniGatewayPathConfiguration(Section section) {
		super(section);
	}

	@Override
	public String getDestinationHost() {
		return section.get("destinationHost");
	}

	@Override
	public int getDestinationPort() {
		return section.get("destinationPort", Integer.class, 80);
	}

}
