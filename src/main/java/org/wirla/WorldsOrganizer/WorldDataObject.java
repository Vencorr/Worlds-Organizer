package org.wirla.WorldsOrganizer;

public class WorldDataObject {

	private int classID;
	private int objectID;
	private int type;
	private String typeName;
	private int version;
	private String label;
	private String value;

	WorldDataObject(int t, int v, String l, String c) {
		type = t;
		typeName = getTypeString(type);
		version = v;
		label = l;
		value = c;
	}

	public static int getTypeInt(String type) {
		switch (type) {
			default:
				return 0;
			case "NET.worlds.console.SavedAvMenuItem":
				return 1;
			case "NET.worlds.console.BookmarkMenuItem":
				return 2;
			case "NET.worlds.scape.Library":
				return 3;
		}
	}

	public static String getTypeString(int type) {
		switch (type) {
			default:
				Console.sendOutput("For some reason, the type data isn't valid.");
				return null;
			case 1:
				return "NET.worlds.console.SavedAvMenuItem";
			case 2:
				return "NET.worlds.console.BookmarkMenuItem";
			case 3:
				return "NET.worlds.scape.Library";
		}
	}

	public static boolean isType(String s) {
		return getTypeInt(s) != 0;
	}

	public String getLabel() {
		return label;
	}

	public String getValue() {
		return value;
	}

	public void setLabel(String s) { label = s; }

	public void setValue(String s) { value = s; }

	public static WorldDataObject newType(int i) {
		switch (i) {
			default:
				return new WorldDataObject(i, 1, "New Object", "");
			case 1:
				return new WorldDataObject(i, 1, "New Avatar", "avatar:holden.mov");
			case 2:
				return new WorldDataObject(i, 1, "New Mark", "home:GroundZero/groundzero.world");
		}
	}
}
