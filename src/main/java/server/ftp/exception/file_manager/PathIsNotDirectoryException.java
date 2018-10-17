package server.ftp.exception.file_manager;

import java.nio.file.Path;

public class PathIsNotDirectoryException extends PathException {

	public PathIsNotDirectoryException(Path path) {
		super(path);
	}
	
}
