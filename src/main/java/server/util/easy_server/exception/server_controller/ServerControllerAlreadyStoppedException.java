package server.util.easy_server.exception.server_controller;

public class ServerControllerAlreadyStoppedException extends ServerControllerException {

	public ServerControllerAlreadyStoppedException() {
	}

	public ServerControllerAlreadyStoppedException(String message) {
		super(message);
	}

	public ServerControllerAlreadyStoppedException(Throwable cause) {
		super(cause);
	}

	public ServerControllerAlreadyStoppedException(String message, Throwable cause) {
		super(message, cause);
	}

	public ServerControllerAlreadyStoppedException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
