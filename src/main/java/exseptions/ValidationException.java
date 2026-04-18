package exseptions;

public class ValidationException extends RuntimeException {

    private ErrorCode errorCode;

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public ValidationException(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }
    public ValidationException(String message) {
    }
}