package com.aalto.paycraft.exception;

import com.aalto.paycraft.dto.ErrorResponseDto;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private ErrorResponseDto buildError(WebRequest request, HttpStatus status, String message) {
        return ErrorResponseDto.builder()
                .apiPath(request.getDescription(false))
                .errorCode(status)
                .errorMsg(message)
                .errorTime(LocalDateTime.now())
                .build();
    }

    // -------------------------------------------------------------------------
    // Domain exceptions
    // -------------------------------------------------------------------------

    @ExceptionHandler(EmployerNotFound.class)
    public ResponseEntity<ErrorResponseDto> handleEmployerNotFound(EmployerNotFound ex, WebRequest request) {
        log.warn("Resource not found [{}]: {}", request.getDescription(false), ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildError(request, HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    @ExceptionHandler(EmployerAlreadyExists.class)
    public ResponseEntity<ErrorResponseDto> handleEmployerAlreadyExists(EmployerAlreadyExists ex, WebRequest request) {
        log.warn("Conflict [{}]: {}", request.getDescription(false), ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildError(request, HttpStatus.CONFLICT, ex.getMessage()));
    }

    @ExceptionHandler(PasswordUpdateException.class)
    public ResponseEntity<ErrorResponseDto> handlePasswordUpdate(PasswordUpdateException ex, WebRequest request) {
        log.warn("Password update rejected [{}]: {}", request.getDescription(false), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildError(request, HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    // -------------------------------------------------------------------------
    // JWT / security exceptions
    // -------------------------------------------------------------------------

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorResponseDto> handleExpiredJwt(ExpiredJwtException ex, WebRequest request) {
        log.warn("Expired JWT [{}]", request.getDescription(false));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(buildError(request, HttpStatus.UNAUTHORIZED,
                        "JWT has expired — please log in or refresh your token"));
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<ErrorResponseDto> handleJwtSignature(SignatureException ex, WebRequest request) {
        log.error("Invalid JWT signature [{}]", request.getDescription(false));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(buildError(request, HttpStatus.UNAUTHORIZED,
                        "Invalid JWT signature — please log in again"));
    }

    // -------------------------------------------------------------------------
    // Client errors
    // -------------------------------------------------------------------------

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        log.warn("Bad request [{}]: {}", request.getDescription(false), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildError(request, HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDto> handleTypeMismatch(MethodArgumentTypeMismatchException ex, WebRequest request) {
        String message = String.format("Parameter '%s' must be of type %s",
                ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        log.warn("Type mismatch [{}]: {}", request.getDescription(false), message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildError(request, HttpStatus.BAD_REQUEST, message));
    }

    // -------------------------------------------------------------------------
    // Server errors
    // -------------------------------------------------------------------------

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponseDto> handleRuntimeException(RuntimeException ex, WebRequest request) {
        log.error("Unhandled runtime exception [{}]: {}", request.getDescription(false), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildError(request, HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGenericException(Exception ex, WebRequest request) {
        log.error("Unhandled exception [{}]: {}", request.getDescription(false), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildError(request, HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred"));
    }

    // -------------------------------------------------------------------------
    // Spring MVC overrides — standardized to ErrorResponseDto
    // -------------------------------------------------------------------------

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> violations = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> violations.put(err.getField(), err.getDefaultMessage()));
        log.warn("Validation failed [{}]: {}", request.getDescription(false), violations);
        ErrorResponseDto body = ErrorResponseDto.builder()
                .apiPath(request.getDescription(false))
                .errorCode(HttpStatus.BAD_REQUEST)
                .errorMsg("Request validation failed")
                .errorTime(LocalDateTime.now())
                .violations(violations)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String message = "Required parameter '" + ex.getParameterName() + "' is missing";
        log.warn("Missing parameter [{}]: {}", request.getDescription(false), message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildError(request, HttpStatus.BAD_REQUEST, message));
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn("Unreadable request body [{}]: {}", request.getDescription(false), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildError(request, HttpStatus.BAD_REQUEST, "Malformed or unreadable request body"));
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String message = "HTTP method '" + ex.getMethod() + "' is not supported for this endpoint";
        log.warn("Method not allowed [{}]: {}", request.getDescription(false), message);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(buildError(request, HttpStatus.METHOD_NOT_ALLOWED, message));
    }
}
