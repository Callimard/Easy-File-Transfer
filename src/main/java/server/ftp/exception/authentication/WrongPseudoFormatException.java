package server.ftp.exception.authentication;

public class WrongPseudoFormatException extends AuthenticationException {

	public WrongPseudoFormatException() {
	}

	public WrongPseudoFormatException(String arg0) {
		super(arg0);
	}

	public WrongPseudoFormatException(Throwable arg0) {
		super(arg0);
	}

	public WrongPseudoFormatException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public WrongPseudoFormatException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

}
