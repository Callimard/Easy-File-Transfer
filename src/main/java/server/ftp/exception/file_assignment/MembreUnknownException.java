package server.ftp.exception.file_assignment;

public class MembreUnknownException extends Exception {

	public MembreUnknownException() {
	}

	public MembreUnknownException(String message) {
		super(message);
	}

	public MembreUnknownException(Throwable cause) {
		super(cause);
	}

	public MembreUnknownException(String message, Throwable cause) {
		super(message, cause);
	}

	public MembreUnknownException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
