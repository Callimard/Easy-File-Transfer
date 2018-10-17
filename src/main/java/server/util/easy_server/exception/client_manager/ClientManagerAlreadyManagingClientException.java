package server.util.easy_server.exception.client_manager;

public class ClientManagerAlreadyManagingClientException extends ClientManagerException {

	public ClientManagerAlreadyManagingClientException() {
	}

	public ClientManagerAlreadyManagingClientException(String message) {
		super(message);
	}

	public ClientManagerAlreadyManagingClientException(Throwable cause) {
		super(cause);
	}

	public ClientManagerAlreadyManagingClientException(String message, Throwable cause) {
		super(message, cause);
	}

	public ClientManagerAlreadyManagingClientException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
