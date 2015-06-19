package com.github.apetrelli.scafa.config;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Profile.Section;

public class Configuration {
    private Ini ini;
    
    public static Configuration create(String profile) throws InvalidFileFormatException, IOException {
        if (profile == null) {
            profile = "direct";
        }
        Ini ini = new Ini();
        ini.load(new File(System.getProperty("user.home") + "/.scafa/" + profile + ".ini"));
        return new Configuration(ini);
    }
    
    private Configuration(Ini ini) {
        this.ini = ini;
    }
    
    public Section getConfigurationByHost(String host) {
        boolean found = false;
        Iterator<String> keyIt = ini.keySet().iterator();
        Section section = null;
        while (!found && keyIt.hasNext()) {
            section = ini.get(keyIt.next());
            List<String> excludeRegexp = section.getAll("excludeRegexp");
            if (excludeRegexp == null || excludeRegexp.isEmpty()) {
                List<String> exclude = section.getAll("exclude");
                if (exclude == null || exclude.isEmpty()) {
                    section.put("excludeRegexp", "doesnotmatchathing"); // Just to avoid useless re-execution
                    excludeRegexp = section.getAll("excludeRegexp");
                } else {
                    excludeRegexp = exclude.stream().map(t -> createRegexpFromWildcard(t)).collect(Collectors.toList());
                    section.putAll("excludeRegexp", excludeRegexp);
                }
            }
            boolean excluded = excludeRegexp.stream().anyMatch(t -> host.matches(t));
            found = !excluded;
        }
        return found ? section : null;
    }
    
    private String createRegexpFromWildcard(String subject) {
        Pattern regex = Pattern.compile("[^*]+|(\\*)");
        Matcher m = regex.matcher(subject);
        StringBuffer b= new StringBuffer();
        while (m.find()) {
            if(m.group(1) != null) m.appendReplacement(b, ".*");
            else m.appendReplacement(b, "\\\\Q" + m.group(0) + "\\\\E");
        }
        m.appendTail(b);
        return b.toString();        
    }
}
