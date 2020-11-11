package com.github.apetrelli.scafa.web.config.ini;

import org.ini4j.Profile.Section;

import com.github.apetrelli.scafa.web.config.StaticPathConfiguration;

public class AbstractIniStaticPathConfiguration extends AbstractIniPathConfiguration implements StaticPathConfiguration {

	public AbstractIniStaticPathConfiguration(Section section) {
		super(section);
	}

	@Override
	public String getBaseFilesystemPath() {
		return section.get("baseFilesystemPath");
	}

	@Override
	public String getIndexResource() {
		return section.get("indexResource");
	}

}
