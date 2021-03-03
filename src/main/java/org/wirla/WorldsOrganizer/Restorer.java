package org.wirla.WorldsOrganizer;

import java.io.*;

public class Restorer {

	DataInputStream dis;

	File file;
	public WorldListObject listObj;

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

	WorldListObject read() throws IOException, InvalidPersisterException {
		if (dis == null) {
			throw new InvalidPersisterException();
		}

		try {
			if (!readString().equals("PERSISTER Worlds, Inc.")) { // Persister Header
				throw new InvalidPersisterException();
			} else {
				int pVersion = readInt(); // Persister Version
				Console.sendOutput("Persister Version detected as " + pVersion + ".");
				if (pVersion != 7) Console.sendOutput("Version not supported!");

				int count = readInt(); // Vector Count

				readInt(); // Class ID
				return readVector(count);
			}
		} catch (NullPointerException e) {
			throw new InvalidPersisterException();
		}
	}

	private WorldListObject readVector(int count) throws IOException {
		Console.sendOutput("Starting read of " + file.getPath(), true);

		listObj = new WorldListObject();

		oID = readInt(); // Object ID
		String typeText = readString(); // Class Name
		listObj.classType = WorldsType.valueOfClass(typeText);
		if (listObj.classType != null) {
			Console.sendOutput("Detected as supported class. Continuing...", true);

			for (int i = 0; i < count; i++) {
				if (i > 0) readInt();
				int version = readInt();

				WorldList newData = null;
				if (listObj.classType == WorldsType.AVATAR) newData = new AvatarObject(readString(), readString());
				else if (listObj.classType == WorldsType.WORLDSMARK) newData = new MarkObject(readString(), readString());
				assert newData != null;
				listObj.add(newData);
			}
			assert readString().equals("END PERSISTER");
		}

		return listObj;
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
