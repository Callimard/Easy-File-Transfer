package server.ftp.exception;

public class UnRecognizedModeException extends Exception {

	public UnRecognizedModeException() {
	}

	public UnRecognizedModeException(String message) {
		super(message);
	}

	public UnRecognizedModeException(Throwable cause) {
		super(cause);
	}

	public UnRecognizedModeException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnRecognizedModeException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
