package server.ftp.exception;

public class WrongPathDescriptionFormatException extends Exception {

	public WrongPathDescriptionFormatException() {
	}

	public WrongPathDescriptionFormatException(String message) {
		super(message);
	}

	public WrongPathDescriptionFormatException(Throwable cause) {
		super(cause);
	}

	public WrongPathDescriptionFormatException(String message, Throwable cause) {
		super(message, cause);
	}

	public WrongPathDescriptionFormatException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
