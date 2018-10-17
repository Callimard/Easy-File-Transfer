package server.ftp.exception.file_assignment;

public class RootGroupCantBeRemovedException extends RuntimeException {

	public RootGroupCantBeRemovedException() {
	}

	public RootGroupCantBeRemovedException(String message) {
		super(message);
	}

	public RootGroupCantBeRemovedException(Throwable cause) {
		super(cause);
	}

	public RootGroupCantBeRemovedException(String message, Throwable cause) {
		super(message, cause);
	}

	public RootGroupCantBeRemovedException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
