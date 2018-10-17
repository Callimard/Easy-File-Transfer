package server.ftp.exception.authentication;

public class AlreadyPreparedException extends AuthenticationException {

	public AlreadyPreparedException() {
	}

	public AlreadyPreparedException(String arg0) {
		super(arg0);
	}

	public AlreadyPreparedException(Throwable arg0) {
		super(arg0);
	}

	public AlreadyPreparedException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public AlreadyPreparedException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

}
