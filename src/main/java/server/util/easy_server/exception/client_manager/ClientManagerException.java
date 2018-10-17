package server.util.easy_server.exception.client_manager;

public class ClientManagerException extends Exception {

	public ClientManagerException() {
	}

	public ClientManagerException(String message) {
		super(message);
	}

	public ClientManagerException(Throwable cause) {
		super(cause);
	}

	public ClientManagerException(String message, Throwable cause) {
		super(message, cause);
	}

	public ClientManagerException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
