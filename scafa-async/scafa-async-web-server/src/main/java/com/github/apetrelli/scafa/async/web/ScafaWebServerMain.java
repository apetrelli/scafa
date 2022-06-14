package com.github.apetrelli.scafa.async.web;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import lombok.extern.java.Log;

@Log
public class ScafaWebServerMain {

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
                log.log(Level.INFO, "Main thread interrupted", e);
            }
        }
	}

}
