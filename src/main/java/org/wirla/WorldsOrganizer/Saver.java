package org.wirla.WorldsOrganizer;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class Saver {

	DataOutputStream dis;

	File file;
	private File backupFile;

	Saver(File file) throws IOException {
		Console.sendOutput("Initialized Saver", true);
		this.file = file;
		backupFile = new File(file.getAbsolutePath() + ".organizer-bkup");
		try {
			if (file.exists() && !Main.config.attemptBackup) {
				Console.sendOutput("File location already exists! Creating backup at " + backupFile.getAbsolutePath(), true);
				Files.copy(Paths.get(file.getAbsolutePath()), Paths.get(backupFile.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException e) {
			Console.sendOutput("Backup Error", true);
			Dialog.showError("Unable to create backup!", "Saving will try and continue but might corrupt your files if it fails.");
		}

		try {
			FileOutputStream fis = new FileOutputStream(file);
			dis = new DataOutputStream(fis);
		} catch (IOException e) {
			Console.sendOutput("Couldn't write to file!");
			Dialog.showException(e);
		}
		writeString("PERSISTER Worlds, Inc."); // Persister header
		writeInt(7); // Persister version

	}

	public void save(WorldListObject objects) throws IOException {
		int count = objects.size();
		writeInt(count);
		writeInt(459);
		int objID = 8782;
		writeInt(objID);
		writeString(objects.classType.name);
		for (int i = 0; i < count; i++) {
			if (i > 0) {
				writeInt(460 + i);
				writeInt(objID);
			}
			else writeInt(1);
			writeString(objects.get(i).getName());
			writeString(objects.get(i).getValue());
			Console.sendOutput("Saved WorldList item to file: { name: '" + objects.get(i).getName() + "', value: '" + objects.get(i).getValue() + "' }", true);
		}
		writeString("END PERSISTER");
		if (backupFile.exists() && Main.config.attemptBackup) backupFile.delete();
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
