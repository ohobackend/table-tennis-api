package com.tabletennis.app.common.exception;
public enum ErrorCode {
    VALIDATION_ERROR(400,"요청 값 검증 실패"), UNAUTHORIZED(401,"인증이 필요합니다."),
    FORBIDDEN(403,"권한이 없습니다."), NOT_FOUND(404,"리소스를 찾을 수 없습니다."),
    NOTICE_NOT_FOUND(404,"해당 공지사항을 찾을 수 없습니다."), CONFLICT(409,"중복 또는 충돌이 발생했습니다."),
    INTERNAL_ERROR(500,"서버 오류가 발생했습니다.");
    public final int status; public final String message;
    ErrorCode(int status,String message) { this.status=status; this.message=message; }
}
