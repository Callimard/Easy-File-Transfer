package server.util.easy_server.exception.server_controller;

public class ServerControllerFailedToStopException extends ServerControllerException {

	public ServerControllerFailedToStopException() {
	}

	public ServerControllerFailedToStopException(String message) {
		super(message);
	}

	public ServerControllerFailedToStopException(Throwable cause) {
		super(cause);
	}

	public ServerControllerFailedToStopException(String message, Throwable cause) {
		super(message, cause);
	}

	public ServerControllerFailedToStopException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
