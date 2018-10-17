package server.ftp.exception.authentication;

public class AuthenticationManagerAlreadyStoppedException extends AuthenticationManagerException {

	public AuthenticationManagerAlreadyStoppedException() {
	}

	public AuthenticationManagerAlreadyStoppedException(String arg0) {
		super(arg0);
	}

	public AuthenticationManagerAlreadyStoppedException(Throwable arg0) {
		super(arg0);
	}

	public AuthenticationManagerAlreadyStoppedException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public AuthenticationManagerAlreadyStoppedException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

}
