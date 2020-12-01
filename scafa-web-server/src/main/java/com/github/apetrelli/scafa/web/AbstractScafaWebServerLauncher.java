package com.github.apetrelli.scafa.web;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ini4j.Ini;

import com.github.apetrelli.scafa.web.config.Configuration;
import com.github.apetrelli.scafa.web.config.ini.IniConfiguration;

public abstract class AbstractScafaWebServerLauncher {
	
	private static final Logger LOG = Logger.getLogger(AbstractScafaWebServerLauncher.class.getName());
	
	public void launch(String rootFilesystemDirectory) {
		try {
			Ini ini = new Ini(new File(rootFilesystemDirectory, "config.ini"));
			IniConfiguration config = new IniConfiguration(ini, rootFilesystemDirectory,
					getClass().getResource("/mime.types"));
			launch(config);
		} catch (IOException e) {
            LOG.log(Level.SEVERE, "Cannot start web server", e);
		}
	}
	
	public abstract void stop();

	protected abstract void launch(Configuration config);
}
