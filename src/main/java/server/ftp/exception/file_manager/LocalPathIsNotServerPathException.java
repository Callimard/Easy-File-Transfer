package server.ftp.exception.file_manager;

public class LocalPathIsNotServerPathException extends Exception {

	public LocalPathIsNotServerPathException() {
	}

	public LocalPathIsNotServerPathException(String message) {
		super(message);
	}

	public LocalPathIsNotServerPathException(Throwable cause) {
		super(cause);
	}

	public LocalPathIsNotServerPathException(String message, Throwable cause) {
		super(message, cause);
	}

	public LocalPathIsNotServerPathException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
