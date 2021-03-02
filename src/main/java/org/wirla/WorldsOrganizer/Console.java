package org.wirla.WorldsOrganizer;

import java.io.IOException;
import java.util.Properties;

public class Console {

    final static Properties properties = new Properties();

    public static void sendOutput(String message) {
        sendOutput(message, false);
    }

    public static void sendOutput(String message, boolean debug) {
        if (Main.debugMode || !debug) {
            System.out.println(message);
        }
    }

    private static String getProperty(String property) {
        try {
            properties.load(Main.class.getClassLoader().getResourceAsStream("project.properties"));
            return properties.getProperty(property);
        } catch (IOException e) {
            return "?";
        }
    }

    public static String getVersion(){
        return getProperty("version");
    }

    public static String getDate(){
        return getProperty("buildDate");
    }

}
