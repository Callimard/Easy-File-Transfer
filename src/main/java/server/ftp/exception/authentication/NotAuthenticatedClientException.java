package server.ftp.exception.authentication;

public class NotAuthenticatedClientException extends AuthenticationException {

	public NotAuthenticatedClientException() {
	}

	public NotAuthenticatedClientException(String arg0) {
		super(arg0);
	}

	public NotAuthenticatedClientException(Throwable arg0) {
		super(arg0);
	}

	public NotAuthenticatedClientException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public NotAuthenticatedClientException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

}
