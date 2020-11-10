package org.wirla.WorldsOrganizer;

import java.io.*;
import java.util.*;

public class Restorer {

	DataInputStream dis;

	String path;

	private int oID;

	Restorer(String path) {
		this.path = path;
		try {
			FileInputStream fis = new FileInputStream(new File(path));
			dis = new DataInputStream(fis);
		} catch (IOException e) {
			System.out.println("Error reading from file: " +
					e.getMessage());
		}
	}

	List<WorldDataObject> getValues() throws IOException, InvalidPersisterFile {
		if (dis == null) {
			throw new InvalidPersisterFile();
		}

		try {
			if (!readString().equals("PERSISTER Worlds, Inc.")) { // Persister Header
				throw new InvalidPersisterFile();
			} else {
				int pVersion = readInt(); // Persister Version
				System.out.println("Persister Version detected as " + pVersion + ".");
				if (pVersion != 7) System.out.println("Version not supported. This may not work!");

				int count = readInt(); // Vector Count

				readInt(); // Class ID
				return readVector(count);
			}
		} catch (NullPointerException e) {
			throw new InvalidPersisterFile();
		}
	}

	private List<WorldDataObject> readVector(int count) throws IOException {
		System.out.println("Starting Read of " + path);

		List<WorldDataObject> vList = new ArrayList<WorldDataObject>(count);

		oID = readInt(); // Object ID
		String type = readString(); // Class Name
		if (WorldDataObject.isType(type)) {
			System.out.println("Detected as supported class. Continuing.");

			for (int i = 0; i < count; i++) {
				if (i > 0) readInt();
				int version = readInt();
				WorldDataObject curW = new WorldDataObject(type, version, readString(), readString());
				vList.add(curW);
			}
			assert readString().equals("END PERSISTER");
		}

		return vList;
	}

	String readString() throws IOException {
		if (readBoolean()) {
			return null;
		} else {
			return dis.readUTF();
		}
	}

	int readInt() throws IOException {
		return dis.readInt();
	}

	byte readByte() throws IOException {
		return dis.readByte();
	}

	boolean readBoolean() throws IOException {
		return dis.readBoolean();
	}

}
