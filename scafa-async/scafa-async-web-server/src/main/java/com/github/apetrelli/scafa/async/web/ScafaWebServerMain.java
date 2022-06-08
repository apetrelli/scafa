package com.github.apetrelli.scafa.async.web;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ScafaWebServerMain {
	
	private static final Logger LOG = Logger.getLogger(ScafaWebServerMain.class.getName());

	public static void main(String[] args) throws IOException {
		
		ScafaWebServerLauncher launcher = new ScafaWebServerLauncher();
		String rootDirectory;
		if (args.length > 0) {
			rootDirectory = args[0];
		} else {
			rootDirectory = System.getProperty("user.dir");
		}
		launcher.launch(rootDirectory);

		Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                launcher.stop();
            }
        });

		while (true) {
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                LOG.log(Level.INFO, "Main thread interrupted", e);
            }
        }
	}

}
