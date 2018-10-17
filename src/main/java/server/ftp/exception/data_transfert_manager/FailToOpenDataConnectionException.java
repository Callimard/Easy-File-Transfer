package server.ftp.exception.data_transfert_manager;

public class FailToOpenDataConnectionException extends Exception {

	public FailToOpenDataConnectionException() {
	}

	public FailToOpenDataConnectionException(String message) {
		super(message);
	}

	public FailToOpenDataConnectionException(Throwable cause) {
		super(cause);
	}

	public FailToOpenDataConnectionException(String message, Throwable cause) {
		super(message, cause);
	}

	public FailToOpenDataConnectionException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
