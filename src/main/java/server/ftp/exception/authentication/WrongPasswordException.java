package server.ftp.exception.authentication;

public class WrongPasswordException extends AuthenticationException {

	public WrongPasswordException() {
	}

	public WrongPasswordException(String arg0) {
		super(arg0);
	}

	public WrongPasswordException(Throwable arg0) {
		super(arg0);
	}

	public WrongPasswordException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public WrongPasswordException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

}
