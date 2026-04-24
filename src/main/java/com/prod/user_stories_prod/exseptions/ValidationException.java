package com.prod.user_stories_prod.exseptions;

import com.prod.user_stories_prod.responses.ErrorCode;

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