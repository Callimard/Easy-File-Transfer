package server.ftp.exception.authentication;

public class FailToDeauthenticateException extends AuthenticationException {

	public FailToDeauthenticateException() {
	}

	public FailToDeauthenticateException(String arg0) {
		super(arg0);
	}

	public FailToDeauthenticateException(Throwable arg0) {
		super(arg0);
	}

	public FailToDeauthenticateException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public FailToDeauthenticateException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

}
