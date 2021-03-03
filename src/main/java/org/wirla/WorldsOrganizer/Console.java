package org.wirla.WorldsOrganizer;

import java.io.IOException;
import java.net.*;
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

    public static String getHelp() {
        String command;
        command = "WorldsOrganizer.jar [OPTIONS]\n" +
                "Commands:\n" +
                commandCreate("-v --verbose", "Enable verbose printouts.") + "\n" +
                commandCreate("-i --input", "Start application with input files.") + "\n" +
                commandCreate("-h --help", "Show this output.");

        return command;
    }

    private static String commandCreate(String cmd, String info) {
        return "     " + cmd + "                         ".substring(cmd.length()) + info;
    }

    public static boolean testURL(String address) throws IOException {
        int responseCode;
        try {
            URL url = new URL(address);
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();
            huc.setRequestMethod("HEAD");
            huc.setConnectTimeout(600);

            responseCode = huc.getResponseCode();
        } catch (SocketTimeoutException | NoRouteToHostException | UnknownHostException e) {
            responseCode = 404;
        }

        return HttpURLConnection.HTTP_OK == responseCode;
    }

}
