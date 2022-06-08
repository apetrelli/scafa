package com.github.apetrelli.scafa.proxy;

public interface ScafaProxyLauncher {

	void initialize();

	String getLastUsedProfile();

	void saveLastUsedProfile(String profile);

	String[] getProfiles();

	void launch(String profile);

	void stop();

}