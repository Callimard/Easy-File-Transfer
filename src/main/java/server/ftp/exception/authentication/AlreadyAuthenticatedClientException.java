package server.ftp.exception.authentication;

public class AlreadyAuthenticatedClientException extends AuthenticationException {

	public AlreadyAuthenticatedClientException() {
	}

	public AlreadyAuthenticatedClientException(String arg0) {
		super(arg0);
	}

	public AlreadyAuthenticatedClientException(Throwable arg0) {
		super(arg0);
	}

	public AlreadyAuthenticatedClientException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public AlreadyAuthenticatedClientException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

}
