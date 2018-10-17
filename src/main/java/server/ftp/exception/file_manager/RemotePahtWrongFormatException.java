package server.ftp.exception.file_manager;

public class RemotePahtWrongFormatException extends Exception {

	public RemotePahtWrongFormatException() {
	}

	public RemotePahtWrongFormatException(String message) {
		super(message);
	}

	public RemotePahtWrongFormatException(Throwable cause) {
		super(cause);
	}

	public RemotePahtWrongFormatException(String message, Throwable cause) {
		super(message, cause);
	}

	public RemotePahtWrongFormatException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
