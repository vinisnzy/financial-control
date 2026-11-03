package com.vinisnzy.financial_control_api.exceptions.handler;

import com.vinisnzy.financial_control_api.exceptions.BusinessException;
import com.vinisnzy.financial_control_api.exceptions.ErrorCode;
import com.vinisnzy.financial_control_api.exceptions.NotFoundException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, @NonNull HttpHeaders headers, @NonNull HttpStatusCode status, @NonNull WebRequest request) {
        ProblemDetail pd = build(HttpStatus.BAD_REQUEST,
                "Validation Error",
                "VALIDATION_ERROR",
                "One or more fields are invalid.");
        pd.setProperty("errors", ex.getBindingResult().getFieldErrors().stream().map(
                e -> Map.of("field", e.getField(), "message", Objects.requireNonNullElse(e.getDefaultMessage(), "Invalid")
                )).toList());
        return ResponseEntity.badRequest().body(pd);
    }

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFoundException(NotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "Resource Not Found", "RESOURCE_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleBusinessException(BusinessException ex) {
        ErrorCode c = ex.getCode();
        return build(c.getStatus(), c.getTitle(), c.name(), ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation: {}", ex.getMostSpecificCause().getMessage());
        return build(HttpStatus.CONFLICT, "Data Integrity Violation", "DATA_INTEGRITY_VIOLATION", "The operation conflicts with existing data.");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
        ProblemDetail pd = build(HttpStatus.BAD_REQUEST, "Validation Error",
                "VALIDATION_ERROR", "One or more parameters are invalid.");
        pd.setProperty("errors", ex.getConstraintViolations().stream()
                .map(v -> Map.of("field", lastNode(v.getPropertyPath()),
                        "message", v.getMessage()))
                .toList());
        return pd;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String expected = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "valid type";
        return build(HttpStatus.BAD_REQUEST, "Invalid Parameter", "INVALID_PARAMETER",
                "Parameter '%s' must be of type %s.".formatted(ex.getName(), expected));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, "Forbidden", "ACCESS_DENIED",
                "You do not have permission to perform this action.");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        log.error("Unexpected Error", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Error", "INTERNAL_ERROR", "Unexpected Error, try later again.");
    }

    private ProblemDetail build(HttpStatus status, String title, String code, String detail) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        pd.setProperty("code", code);
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    private String lastNode(Path path) {
        String last = null;
        for (Path.Node n : path) last = n.getName();
        return last;
    }
}
