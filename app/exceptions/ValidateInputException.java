package exceptions;

public class ValidateInputException extends Exception {

	private static final long serialVersionUID = 1L;

	private int code;

	public ValidateInputException(String message) {
		super(message);
		this.code = 0;
	}
	
	public ValidateInputException(String message, int code) {
		super(message);
		this.code = code;
	}

	public ValidateInputException(String message, Throwable cause, int code) {
		super(message, cause);
		this.code = code;
	}

	public int getCode() {
		return this.code;
	}

}
