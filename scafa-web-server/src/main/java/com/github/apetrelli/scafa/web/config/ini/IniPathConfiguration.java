package com.github.apetrelli.scafa.web.config.ini;

import org.ini4j.Profile.Section;

import com.github.apetrelli.scafa.web.config.PathConfiguration;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class IniPathConfiguration implements PathConfiguration {

	protected final Section section;

	@Override
	public String getBasePath() {
		return section.get("basePath");
	}

}
