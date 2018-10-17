package server.ftp.exception.file_manager;

import java.nio.file.Path;

public class ClearWorkspaceDirectoryFailedException extends PathException {

	public ClearWorkspaceDirectoryFailedException(Path path) {
		super(path);
	}

}
