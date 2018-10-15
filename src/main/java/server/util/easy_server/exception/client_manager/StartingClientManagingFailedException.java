package server.util.easy_server.exception.client_manager;

public class StartingClientManagingFailedException extends ClientManagerException {

	public StartingClientManagingFailedException() {
	}

	public StartingClientManagingFailedException(String message) {
		super(message);
	}

	public StartingClientManagingFailedException(Throwable cause) {
		super(cause);
	}

	public StartingClientManagingFailedException(String message, Throwable cause) {
		super(message, cause);
	}

	public StartingClientManagingFailedException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
