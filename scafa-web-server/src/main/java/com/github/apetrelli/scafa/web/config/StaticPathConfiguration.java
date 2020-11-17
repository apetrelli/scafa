package com.github.apetrelli.scafa.web.config;

public interface StaticPathConfiguration extends PathConfiguration {
	String getBaseFilesystemPath();
	
	String getIndexResource();
}
