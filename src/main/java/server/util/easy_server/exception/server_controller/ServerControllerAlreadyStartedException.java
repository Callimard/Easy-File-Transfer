package server.util.easy_server.exception.server_controller;

public class ServerControllerAlreadyStartedException extends ServerControllerException {

	public ServerControllerAlreadyStartedException() {
	}

	public ServerControllerAlreadyStartedException(String message) {
		super(message);
	}

	public ServerControllerAlreadyStartedException(Throwable cause) {
		super(cause);
	}

	public ServerControllerAlreadyStartedException(String message, Throwable cause) {
		super(message, cause);
	}

	public ServerControllerAlreadyStartedException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
