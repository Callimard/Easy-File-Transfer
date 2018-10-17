package server.ftp.exception.data_transfert_manager;

public class DataConnectionAlreadyOpenForClientConnectionException extends Exception {

	public DataConnectionAlreadyOpenForClientConnectionException() {
	}

	public DataConnectionAlreadyOpenForClientConnectionException(String message) {
		super(message);
	}

	public DataConnectionAlreadyOpenForClientConnectionException(Throwable cause) {
		super(cause);
	}

	public DataConnectionAlreadyOpenForClientConnectionException(String message, Throwable cause) {
		super(message, cause);
	}

	public DataConnectionAlreadyOpenForClientConnectionException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
