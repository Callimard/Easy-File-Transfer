package server.ftp.exception.file_assignment;

public class GroupDoesNotExistsException extends Exception {

	public GroupDoesNotExistsException() {
	}

	public GroupDoesNotExistsException(String message) {
		super(message);
	}

	public GroupDoesNotExistsException(Throwable cause) {
		super(cause);
	}

	public GroupDoesNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public GroupDoesNotExistsException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
