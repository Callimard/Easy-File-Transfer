package server.ftp.exception;

public class UnRecognizedStructureException extends Exception {

	public UnRecognizedStructureException() {
	}

	public UnRecognizedStructureException(String message) {
		super(message);
	}

	public UnRecognizedStructureException(Throwable cause) {
		super(cause);
	}

	public UnRecognizedStructureException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnRecognizedStructureException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
