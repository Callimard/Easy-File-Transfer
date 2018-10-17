package server.ftp.exception.file_manager;

import java.nio.file.Path;

public class PathIsNotFileException extends PathException {

	public PathIsNotFileException(String string) {
		super(string);
	}

	public PathIsNotFileException(Path path) {
		super(path);
	}

}
