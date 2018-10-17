package server.ftp.exception.file_manager;

import java.nio.file.Path;

public class PathDoesNotExistException extends PathException {

	// Variables.
	
	// Constructors.
	
	public PathDoesNotExistException(String string) {
		super(string);
	}
	
	public PathDoesNotExistException(Path path) {
		super(path);
	}
	
	// Methods.

}
