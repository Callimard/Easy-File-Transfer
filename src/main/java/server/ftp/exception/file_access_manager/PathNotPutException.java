package server.ftp.exception.file_access_manager;

public class PathNotPutException extends Exception {

	public PathNotPutException() {
	}

	public PathNotPutException(String message) {
		super(message);
	}

	public PathNotPutException(Throwable cause) {
		super(cause);
	}

	public PathNotPutException(String message, Throwable cause) {
		super(message, cause);
	}

	public PathNotPutException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
