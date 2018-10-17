package server.ftp.exception;

public class FatalErrorException extends Exception {

	public FatalErrorException() {
	}

	public FatalErrorException(String message) {
		super(message);
	}

	public FatalErrorException(Throwable cause) {
		super(cause);
	}

	public FatalErrorException(String message, Throwable cause) {
		super(message, cause);
	}

	public FatalErrorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
