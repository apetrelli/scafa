package com.github.apetrelli.scafa.proxy;

import java.io.File;
import java.io.IOException;

import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

public class ConfigurationUtils {

    private ConfigurationUtils() {
    }

    public static Ini loadIni(String profile) throws IOException, InvalidFileFormatException {
        Ini ini = new Ini();
        ini.load(getFile(profile));
        return ini;
    }

    public static void saveIni(Ini ini, String profile) throws IOException {
        ini.store(getFile(profile));
    }

    public static void delete(String profile) {
        getFile(profile).delete();
    }

    private static File getFile(String profile) {
        return new File(System.getProperty("user.home") + "/.scafa/" + profile + ".ini");
    }
}
