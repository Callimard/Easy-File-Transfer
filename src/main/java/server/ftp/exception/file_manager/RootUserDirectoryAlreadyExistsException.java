package server.ftp.exception.file_manager;

public class RootUserDirectoryAlreadyExistsException extends UserException {

	public RootUserDirectoryAlreadyExistsException() {
		super();
	}

	public RootUserDirectoryAlreadyExistsException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public RootUserDirectoryAlreadyExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public RootUserDirectoryAlreadyExistsException(String message) {
		super(message);
	}

	public RootUserDirectoryAlreadyExistsException(Throwable cause) {
		super(cause);
	}

}
