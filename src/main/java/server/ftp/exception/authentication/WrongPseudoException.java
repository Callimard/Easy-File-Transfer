package server.ftp.exception.authentication;

public class WrongPseudoException extends AuthenticationException {

	public WrongPseudoException() {
	}

	public WrongPseudoException(String arg0) {
		super(arg0);
	}

	public WrongPseudoException(Throwable arg0) {
		super(arg0);
	}

	public WrongPseudoException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public WrongPseudoException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

}
