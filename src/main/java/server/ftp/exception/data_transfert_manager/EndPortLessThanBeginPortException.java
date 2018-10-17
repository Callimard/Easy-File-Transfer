package server.ftp.exception.data_transfert_manager;

public class EndPortLessThanBeginPortException extends Exception {

	public EndPortLessThanBeginPortException() {
	}

	public EndPortLessThanBeginPortException(String message) {
		super(message);
	}

	public EndPortLessThanBeginPortException(Throwable cause) {
		super(cause);
	}

	public EndPortLessThanBeginPortException(String message, Throwable cause) {
		super(message, cause);
	}

	public EndPortLessThanBeginPortException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
