package org.wirla.WorldsOrganizer;

import java.io.IOException;

public class InvalidPersisterException extends IOException {

	public InvalidPersisterException() {
		super();
	}

	public InvalidPersisterException(String message) {
		super(message);
	}

	public InvalidPersisterException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidPersisterException(Throwable cause) {
		super(cause);
	}

}
