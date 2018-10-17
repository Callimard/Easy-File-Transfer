package server.ftp.exception.file_manager;

import java.nio.file.Path;

public class NoFileException extends PathException {

	// Constructors.

	public NoFileException(Path path) {
		super(path);
	}

}
