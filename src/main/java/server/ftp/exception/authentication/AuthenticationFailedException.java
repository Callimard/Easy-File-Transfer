package server.ftp.exception.authentication;

public class AuthenticationFailedException extends AuthenticationException {

	public AuthenticationFailedException() {
	}

	public AuthenticationFailedException(String arg0) {
		super(arg0);
	}

	public AuthenticationFailedException(Throwable arg0) {
		super(arg0);
	}

	public AuthenticationFailedException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public AuthenticationFailedException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

}
