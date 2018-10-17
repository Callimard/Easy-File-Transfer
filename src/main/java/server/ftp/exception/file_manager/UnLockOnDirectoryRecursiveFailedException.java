package server.ftp.exception.file_manager;

import java.nio.file.Path;

public class UnLockOnDirectoryRecursiveFailedException extends PathException {

	public UnLockOnDirectoryRecursiveFailedException(Path path) {
		super(path);
	}
	
}
