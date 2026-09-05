package com.tabletennis.app.common.exception;
public class ApiException extends RuntimeException {
    public final ErrorCode code;
    public ApiException(ErrorCode code) { super(code.message); this.code=code; }
    public ApiException(ErrorCode code,String message) { super(message); this.code=code; }
}
