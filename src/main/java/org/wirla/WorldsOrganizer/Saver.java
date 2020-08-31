package org.wirla.WorldsOrganizer;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class Saver {

	DataOutputStream dis;

	String path;

	Saver(String path) throws IOException {
		this.path = path;
		try {
			FileOutputStream fis = new FileOutputStream(new File(path));
			dis = new DataOutputStream(fis);
		} catch (IOException e) {
			System.out.println("Error writing to file: " +
					e.getMessage());
		}
		writeString("PERSISTER Worlds, Inc."); // Persister header
		writeInt(7); // Persister version
	}

	public void saveAvatars(List<WObject> objects) throws IOException {
		int count = objects.size();
		writeInt(count);
		writeInt(459);
		int objID = 8782;
		writeInt(objID);
		writeString("NET.worlds.console.SavedAvMenuItem");
		for (int i = 0; i < count; i++) {
			if (i > 0) {
				writeInt(460 + i);
				writeInt(objID);
			}
			else writeInt(1);
			writeString(objects.get(i).label);
			writeString(objects.get(i).value);
		}
		writeString("END PERSISTER");
	}

	public void saveMark(List<WObject> objects) throws IOException {
		int count = objects.size();
		writeInt(count);
		writeInt(459);
		int objID = 8782;
		writeInt(objID);
		writeString("NET.worlds.console.BookmarkMenuItem");
		for (int i = 0; i < count; i++) {
			if (i > 0) {
				writeInt(460 + i);
				writeInt(objID);
			}
			else writeInt(1);
			writeString(objects.get(i).label);
			writeString(objects.get(i).value);
		}
		writeString("END PERSISTER");
	}

	void writeString(String s) throws IOException {
		if (!s.isEmpty()) {
			writeBoolean(false);
			dis.writeUTF(s);
		} else {
			writeBoolean(true);
		}
	}

	void writeInt(int i) throws IOException {
		dis.writeInt(i);
	}

	void writeByte(byte b) throws IOException {
		dis.writeByte(b);
	}

	void writeBoolean(boolean b) throws IOException {
		dis.writeBoolean(b);
	}

}
