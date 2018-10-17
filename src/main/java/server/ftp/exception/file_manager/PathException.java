package server.ftp.exception.file_manager;

import java.nio.file.Path;

public class PathException extends Exception {

	// Variables.

	private Path path;

	// Constructors.

	public PathException(String string) {
		super(string);
	}
	
	public PathException(Path path) {
		super(path.toString());
		this.path = path;
	}

	// Methods.

	public Path getPath() {
		return this.path;
	}

}
