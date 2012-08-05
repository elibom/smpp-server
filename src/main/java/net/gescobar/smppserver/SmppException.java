package net.gescobar.smppserver;

public class SmppException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public SmppException() {
		super();
	}

	public SmppException(String message, Throwable cause) {
		super(message, cause);
	}

	public SmppException(String message) {
		super(message);
	}

	public SmppException(Throwable cause) {
		super(cause);
	}
	
}
