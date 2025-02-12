package org.example.version;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Baoyi Chen
 */
public class Version {
    
    static {
        ClassLoader loader = Version.class.getClassLoader();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(loader.getResourceAsStream(".version")))) {
            String version = reader.readLine();
            if (version != null) System.setProperty("trans.version", version);
        } catch (IOException e) {
        }
    }
    
    public static final Version INSTANCE = new Version();
    
    private Version() {
    }
    
    public String version() {
        return System.getProperty("trans.version");
    }
    
    public String home() {
        return System.getProperty("trans.home");
    }
}
