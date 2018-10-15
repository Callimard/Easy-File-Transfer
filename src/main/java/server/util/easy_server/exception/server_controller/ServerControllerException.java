package server.util.easy_server.exception.server_controller;

public class ServerControllerException extends Exception {

	public ServerControllerException() {
	}

	public ServerControllerException(String message) {
		super(message);
	}

	public ServerControllerException(Throwable cause) {
		super(cause);
	}

	public ServerControllerException(String message, Throwable cause) {
		super(message, cause);
	}

	public ServerControllerException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
