package com.github.apetrelli.scafa.web.config.ini;

import org.ini4j.Profile.Section;

import com.github.apetrelli.scafa.web.config.StaticPathConfiguration;

public class AbstractIniStaticPathConfiguration extends AbstractIniPathConfiguration implements StaticPathConfiguration {

	private String rootFilesystemPath;
	
	public AbstractIniStaticPathConfiguration(Section section, String rootFilesystemPath) {
		super(section);
		this.rootFilesystemPath = rootFilesystemPath;
	}

	@Override
	public String getBaseFilesystemPath() {
		String baseFilesystemPath = section.get("baseFilesystemPath");
		return (baseFilesystemPath.startsWith("/") ? "" : rootFilesystemPath)
				+ (rootFilesystemPath.endsWith("/") ? "" : "/") + baseFilesystemPath;
	}

	@Override
	public String getIndexResource() {
		return section.get("indexResource", "index.html");
	}

}
