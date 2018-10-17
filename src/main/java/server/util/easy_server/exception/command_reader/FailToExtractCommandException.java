package server.util.easy_server.exception.command_reader;

public class FailToExtractCommandException extends CommandExtractorException {

	public FailToExtractCommandException() {
	}

	public FailToExtractCommandException(String arg0) {
		super(arg0);
	}

	public FailToExtractCommandException(Throwable arg0) {
		super(arg0);
	}

	public FailToExtractCommandException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public FailToExtractCommandException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

}
