package org.wirla.WorldsOrganizer;

public class InvalidPersisterFile extends Exception {

	public InvalidPersisterFile() {
		super();
	}

	public InvalidPersisterFile(String message) {
		super(message);
	}

	public InvalidPersisterFile(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidPersisterFile(Throwable cause) {
		super(cause);
	}

}
