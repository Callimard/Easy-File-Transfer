package server.ftp.exception.command_executor;

public class RunnerThreadAlreadyStartedException extends Exception {

	public RunnerThreadAlreadyStartedException() {
	}

	public RunnerThreadAlreadyStartedException(String message) {
		super(message);
	}

	public RunnerThreadAlreadyStartedException(Throwable cause) {
		super(cause);
	}

	public RunnerThreadAlreadyStartedException(String message, Throwable cause) {
		super(message, cause);
	}

	public RunnerThreadAlreadyStartedException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
