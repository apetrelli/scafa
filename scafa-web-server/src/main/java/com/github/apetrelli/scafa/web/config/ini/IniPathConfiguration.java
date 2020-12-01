package com.github.apetrelli.scafa.web.config.ini;

import org.ini4j.Profile.Section;

import com.github.apetrelli.scafa.web.config.PathConfiguration;

public class IniPathConfiguration implements PathConfiguration {

	protected Section section;

	public IniPathConfiguration(Section section) {
		this.section = section;
	}

	@Override
	public String getBasePath() {
		return section.get("basePath");
	}

}
