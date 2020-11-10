package org.wirla.WorldsOrganizer;

public class WorldDataObject {

	int classID;
	int objectID;
	String type;
	int version;
	String label;
	String value;

	private static String typeAvatar = "NET.worlds.console.SavedAvMenuItem";
	private static String typeMark = "NET.worlds.console.BookmarkMenuItem";
	private static String typeLibrary = "NET.worlds.scape.Library";

	WorldDataObject(String type, int v, String l, String c) {
		this.type = type;
		version = v;
		label = l;
		value = c;
	}

	public static boolean isType(String s) {
		return s.equals(typeAvatar) || s.equals(typeMark) || s.equals(typeLibrary);
	}

	public static String returnAvatar() {
		return typeAvatar;
	}

	public static String returnMark() {
		return typeMark;
	}

	public static String returnLibrary() {
		return typeLibrary;
	}
}
