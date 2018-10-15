package server.util.easy_server.exception.client_manager;

public class ClientManagerNotManagingClientException extends ClientManagerException {

	public ClientManagerNotManagingClientException() {
	}

	public ClientManagerNotManagingClientException(String message) {
		super(message);
	}

	public ClientManagerNotManagingClientException(Throwable cause) {
		super(cause);
	}

	public ClientManagerNotManagingClientException(String message, Throwable cause) {
		super(message, cause);
	}

	public ClientManagerNotManagingClientException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
