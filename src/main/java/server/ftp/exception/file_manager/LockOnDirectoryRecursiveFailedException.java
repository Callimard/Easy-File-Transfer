package server.ftp.exception.file_manager;

import java.nio.file.Path;

public class LockOnDirectoryRecursiveFailedException extends PathException {

	public LockOnDirectoryRecursiveFailedException(Path path) {
		super(path);
	}

}
