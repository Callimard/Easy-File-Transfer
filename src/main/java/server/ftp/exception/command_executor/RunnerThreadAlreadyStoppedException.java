package server.ftp.exception.command_executor;

public class RunnerThreadAlreadyStoppedException extends Exception {

	public RunnerThreadAlreadyStoppedException() {
	}

	public RunnerThreadAlreadyStoppedException(String message) {
		super(message);
	}

	public RunnerThreadAlreadyStoppedException(Throwable cause) {
		super(cause);
	}

	public RunnerThreadAlreadyStoppedException(String message, Throwable cause) {
		super(message, cause);
	}

	public RunnerThreadAlreadyStoppedException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
