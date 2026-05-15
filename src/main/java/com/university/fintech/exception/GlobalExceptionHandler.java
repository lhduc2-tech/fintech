package com.university.fintech.exception;

import com.university.fintech.exception.domain.AuthException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<Map<String, Object>> handleAuthException(AuthException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getErrorCode(), ex.getMessage(), Collections.emptyMap());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed.", Map.copyOf(errors));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        Map<String, Object> details = new HashMap<>();
        details.put("rootCause", safeMessage(ex.getMostSpecificCause()));
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "DATA_INTEGRITY_VIOLATION", resolveDataIntegrityMessage(ex), details);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> details = new LinkedHashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            details.put(violation.getPropertyPath().toString(), violation.getMessage());
        }
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed.", Map.copyOf(details));
    }

    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<Map<String, Object>> handleTransactionSystemException(TransactionSystemException ex) {
        log.error("Unhandled transaction system exception", ex);
        Throwable root = ex.getRootCause();
        if (root instanceof DataIntegrityViolationException dataIntegrityViolationException) {
            return handleDataIntegrityViolation(dataIntegrityViolationException);
        }
        if (root instanceof ConstraintViolationException constraintViolationException) {
            return handleConstraintViolation(constraintViolationException);
        }

        Map<String, Object> details = new HashMap<>();
        details.put("exception", ex.getClass().getSimpleName());
        details.put("rootCause", safeMessage(root));
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", safeUserMessage(ex), details);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        Map<String, Object> details = new HashMap<>();
        details.put("exception", ex.getClass().getSimpleName());
        details.put("rootCause", safeMessage(ex));
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_REQUEST_PAYLOAD", "Request body is invalid or malformed.", details);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(AccessDeniedException ex) {
        Map<String, Object> details = new HashMap<>();
        details.put("exception", ex.getClass().getSimpleName());
        return buildErrorResponse(HttpStatus.FORBIDDEN, "FORBIDDEN", "You do not have permission to access this resource.", details);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        Map<String, Object> details = new HashMap<>();
        details.put("exception", ex.getClass().getSimpleName());
        details.put("method", ex.getMethod());
        details.put("supportedMethods", ex.getSupportedHttpMethods() == null ? java.util.List.of() : ex.getSupportedHttpMethods().stream().map(org.springframework.http.HttpMethod::name).toList());
        return buildErrorResponse(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", "HTTP method is not supported for this endpoint.", details);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest request) {
        if (request != null && "POST".equalsIgnoreCase(request.getMethod()) && request.getRequestURI() != null && request.getRequestURI().endsWith("/users")) {
            Map<String, Object> details = new HashMap<>();
            details.put("exception", ex.getClass().getSimpleName());
            details.put("method", request.getMethod());
            details.put("path", request.getRequestURI());
            return buildErrorResponse(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", "HTTP method is not supported for this endpoint.", details);
        }

        Map<String, Object> details = new HashMap<>();
        details.put("exception", ex.getClass().getSimpleName());
        details.put("path", request == null ? "" : request.getRequestURI());
        return buildErrorResponse(HttpStatus.NOT_FOUND, "NOT_FOUND", "The requested resource was not found.", details);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", safeUserMessage(ex), Collections.emptyMap());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Unhandled exception", ex);
        Map<String, Object> details = new HashMap<>();
        details.put("exception", ex.getClass().getSimpleName());
        details.put("rootCause", safeMessage(ex.getCause()));
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", safeUserMessage(ex), details);
    }

    private String resolveDataIntegrityMessage(DataIntegrityViolationException ex) {
        String message = safeMessage(ex.getMostSpecificCause()).toLowerCase();

        if ((message.contains("username") && message.contains("unique")) || message.contains("users_username_key")) {
            return "Username already exists.";
        }
        if ((message.contains("email") && message.contains("unique")) || message.contains("users_email_key")) {
            return "Email already exists.";
        }
        if (message.contains("status") && message.contains("null")) {
            return "User status is required.";
        }

        return "Database constraint violation.";
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            HttpStatus status,
            String error,
            String message,
            Map<String, ?> details
    ) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("error", error);
        response.put("message", message);
        response.put("details", details == null ? Collections.emptyMap() : details);
        return new ResponseEntity<>(response, status);
    }

    private String safeMessage(Throwable throwable) {
        if (throwable == null || throwable.getMessage() == null) {
            return "";
        }
        return throwable.getMessage();
    }

    private String safeUserMessage(Exception ex) {
        String message = ex.getMessage();
        if (message == null || message.isBlank()) {
            return "An unexpected error occurred.";
        }
        return message;
    }
}