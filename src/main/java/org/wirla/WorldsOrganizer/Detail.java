package org.wirla.WorldsOrganizer;

import java.io.IOException;
import java.util.Properties;

public class Detail {

	public static String getVersion(){
		try {
			final Properties properties = new Properties();
			properties.load(Main.class.getClassLoader().getResourceAsStream("project.properties"));
			return properties.getProperty("version");
		} catch (IOException e) {
			return "Error!";
		}
	}

	public static String getDate(){
		try {
			final Properties properties = new Properties();
			properties.load(Main.class.getClassLoader().getResourceAsStream("project.properties"));
			return properties.getProperty("buildDate");
		} catch (IOException e) {
			return "Error!";
		}
	}
}
