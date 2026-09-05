package com.tabletennis.app.common.exception;
import com.tabletennis.app.common.response.ApiResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.dao.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.*;
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log=LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<?> api(ApiException e) { return response(e.code,e.getMessage()); }
    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class,
        MethodArgumentTypeMismatchException.class, ConstraintViolationException.class,
        HandlerMethodValidationException.class, org.springframework.web.bind.MissingServletRequestParameterException.class})
    public ResponseEntity<?> validation(Exception e) { return response(ErrorCode.VALIDATION_ERROR,ErrorCode.VALIDATION_ERROR.message); }
    @ExceptionHandler({DataIntegrityViolationException.class, OptimisticLockingFailureException.class, PessimisticLockingFailureException.class})
    public ResponseEntity<?> conflict(Exception e) { return response(ErrorCode.CONFLICT,ErrorCode.CONFLICT.message); }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> forbidden(Exception e) { return response(ErrorCode.FORBIDDEN,ErrorCode.FORBIDDEN.message); }
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> unauthorized(Exception e) { return response(ErrorCode.UNAUTHORIZED,ErrorCode.UNAUTHORIZED.message); }
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<?> missing(Exception e) { return response(ErrorCode.NOT_FOUND,ErrorCode.NOT_FOUND.message); }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> unexpected(Exception e) { log.error("Unhandled request failure",e); return response(ErrorCode.INTERNAL_ERROR,ErrorCode.INTERNAL_ERROR.message); }
    private ResponseEntity<?> response(ErrorCode code,String message) { return ResponseEntity.status(code.status).body(ApiResponse.fail(code.name(),message)); }
}
