package server.ftp.exception.file_manager;

public class UserHasNotWorkspaceDirectoryException extends UserException {

	public UserHasNotWorkspaceDirectoryException() {
		super();
	}

	public UserHasNotWorkspaceDirectoryException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public UserHasNotWorkspaceDirectoryException(String message, Throwable cause) {
		super(message, cause);
	}

	public UserHasNotWorkspaceDirectoryException(String message) {
		super(message);
	}

	public UserHasNotWorkspaceDirectoryException(Throwable cause) {
		super(cause);
	}

}
