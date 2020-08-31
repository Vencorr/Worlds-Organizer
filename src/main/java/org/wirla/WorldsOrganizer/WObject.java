package org.wirla.WorldsOrganizer;

public class WObject {

	int classID;
	int objectID;
	String type;
	int version;
	String label;
	String value;

	WObject(String type, int v, String l, String c) {
		this.type = type;
		version = v;
		label = l;
		value = c;
	}
}
