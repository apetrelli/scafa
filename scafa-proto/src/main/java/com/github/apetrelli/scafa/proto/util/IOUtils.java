package com.github.apetrelli.scafa.proto.util;

import java.io.Closeable;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IOUtils {

	private static final Logger LOG = Logger.getLogger(IOUtils.class.getName());

	private IOUtils() {
	}

	public static void closeQuietly(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (IOException e) {
				LOG.log(Level.WARNING, "Error during closing", e);
			}
		}

	}
}
