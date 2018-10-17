package server.ftp.exception.file_manager;

public class NoAccessToThisFileException extends Exception {

	public NoAccessToThisFileException() {
	}

	public NoAccessToThisFileException(String message) {
		super(message);
	}

	public NoAccessToThisFileException(Throwable cause) {
		super(cause);
	}

	public NoAccessToThisFileException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoAccessToThisFileException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
