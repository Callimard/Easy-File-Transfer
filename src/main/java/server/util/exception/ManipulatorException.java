package server.util.exception;

public class ManipulatorException extends Exception {

	public ManipulatorException() {
	}

	public ManipulatorException(String message) {
		super(message);
	}

	public ManipulatorException(Throwable cause) {
		super(cause);
	}

	public ManipulatorException(String message, Throwable cause) {
		super(message, cause);
	}

	public ManipulatorException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
