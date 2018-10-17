package server.ftp.exception.authentication;

public class AuthenticationManagerAlreadyStartedException extends AuthenticationManagerException {

	public AuthenticationManagerAlreadyStartedException() {
	}

	public AuthenticationManagerAlreadyStartedException(String arg0) {
		super(arg0);
	}

	public AuthenticationManagerAlreadyStartedException(Throwable arg0) {
		super(arg0);
	}

	public AuthenticationManagerAlreadyStartedException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public AuthenticationManagerAlreadyStartedException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

}
