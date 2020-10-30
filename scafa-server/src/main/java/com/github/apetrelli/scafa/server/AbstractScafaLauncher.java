/**
 * Scafa - A universal non-caching proxy for the road warrior
 * Copyright (C) 2015  Antonio Petrelli
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.apetrelli.scafa.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public abstract class AbstractScafaLauncher {

    private static final Logger LOG = Logger.getLogger(AbstractScafaLauncher.class.getName());
    private File scafaDirectory;

    public void initialize() {
        File home = new File(System.getProperty("user.home"));
        scafaDirectory = new File(home, ".scafa");
        ensureConfigDirectoryPresent(scafaDirectory);
        try (InputStream stream = new FileInputStream(new File(scafaDirectory, "logging.properties"))) {
            LogManager.getLogManager().readConfiguration(stream);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Cannot load logging configuration, exiting", e);
            System.exit(1);
        }
    }

    public String getLastUsedProfile() {
        File file = new File(scafaDirectory, "lastused.txt");
        if (file.exists()) {
            try {
                String profile = FileUtils.readFileToString(file);
                File profileFile = new File(scafaDirectory, profile + ".ini");
                if (profileFile.exists()) {
                    return profile;
                } else {
                    LOG.log(Level.SEVERE, "The file {0}.ini does not exist, defaulting to direct", profile);
                }
            } catch (IOException e) {
                LOG.log(Level.SEVERE, "Cannot load current profile configuration", e);
            }
            return null;
        }
        return "direct";
    }

    public void saveLastUsedProfile(String profile) {
        File file = new File(scafaDirectory, "lastused.txt");
        try {
            FileUtils.write(file, profile);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Cannot write current profile configuration", e);
        }
    }

    public String[] getProfiles() {
        File[] files = scafaDirectory.listFiles((File d, String n ) -> n.endsWith(".ini"));
        String[] profiles = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            String filename = files[i].getName();
            profiles[i] = filename.substring(0, filename.lastIndexOf('.'));
        }
        return profiles;
    }

    public abstract void launch(String profile);

    public abstract void stop();

    private static void ensureConfigDirectoryPresent(File scafaDirectory) {
        if (!scafaDirectory.exists()) {
            scafaDirectory.mkdirs();
            copyToConfigDirectory(scafaDirectory, "direct.ini");
            copyToConfigDirectory(scafaDirectory, "work.ini");
            copyToConfigDirectory(scafaDirectory, "logging.properties");
        }
    }

    private static void copyToConfigDirectory(File scafaDirectory, String filename) {
        File destination = new File(scafaDirectory, filename);
        try (InputStream is = AbstractScafaLauncher.class.getResourceAsStream("/config/" + filename);
                OutputStream os = new FileOutputStream(destination)) {
            IOUtils.copy(is, os);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Cannot transfer file", e);
        }
    }
}
