package server.util.easy_server.exception.server_controller;

public class ServerControllerFailedToStartException extends ServerControllerException {

	public ServerControllerFailedToStartException() {
	}

	public ServerControllerFailedToStartException(String message) {
		super(message);
	}

	public ServerControllerFailedToStartException(Throwable cause) {
		super(cause);
	}

	public ServerControllerFailedToStartException(String message, Throwable cause) {
		super(message, cause);
	}

	public ServerControllerFailedToStartException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
