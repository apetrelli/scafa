package com.github.apetrelli.scafa.web.config.ini;

import org.ini4j.Profile.Section;

import com.github.apetrelli.scafa.web.config.PathConfiguration;

public class AbstractIniPathConfiguration implements PathConfiguration {

	protected Section section;

	public AbstractIniPathConfiguration(Section section) {
		this.section = section;
	}

	@Override
	public String getBasePath() {
		return section.get("basePath");
	}

}
