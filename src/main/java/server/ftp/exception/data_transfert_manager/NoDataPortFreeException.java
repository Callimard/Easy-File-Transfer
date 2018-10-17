package server.ftp.exception.data_transfert_manager;

public class NoDataPortFreeException extends Exception {

	public NoDataPortFreeException() {
	}

	public NoDataPortFreeException(String message) {
		super(message);
	}

	public NoDataPortFreeException(Throwable cause) {
		super(cause);
	}

	public NoDataPortFreeException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoDataPortFreeException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
