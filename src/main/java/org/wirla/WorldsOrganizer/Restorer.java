package org.wirla.WorldsOrganizer;

import java.io.*;
import java.util.*;

public class Restorer {

	DataInputStream dis;

	File file;
	public int type;

	private int oID;

	Restorer(String path) {
		new Restorer(new File(path));
	}

	Restorer(File file) {
		this.file = file;
		try {
			FileInputStream fis = new FileInputStream(file);
			dis = new DataInputStream(fis);
		} catch (IOException e) {
			Console.sendOutput("Error reading from file: " + e.getMessage());
			System.out.println();
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
				Console.sendOutput("Persister Version detected as " + pVersion + ".");
				if (pVersion != 7) Console.sendOutput("Version not supported!");

				int count = readInt(); // Vector Count

				readInt(); // Class ID
				return readVector(count);
			}
		} catch (NullPointerException e) {
			throw new InvalidPersisterFile();
		}
	}

	private List<WorldDataObject> readVector(int count) throws IOException {
		Console.sendOutput("Starting read of " + file.getPath(), true);

		List<WorldDataObject> vList = new ArrayList<WorldDataObject>(count);

		oID = readInt(); // Object ID
		String typeText = readString(); // Class Name
		if (WorldDataObject.isType(typeText)) {
			this.type = WorldDataObject.getTypeInt(typeText);
			Console.sendOutput("Detected as supported class. Continuing...", true);

			for (int i = 0; i < count; i++) {
				if (i > 0) readInt();
				int version = readInt();
				WorldDataObject curW = new WorldDataObject(this.type, version, readString(), readString());
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
