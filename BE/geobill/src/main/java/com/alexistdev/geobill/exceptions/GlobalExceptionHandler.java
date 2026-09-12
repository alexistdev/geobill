package com.alexistdev.geobill.exceptions;

import com.alexistdev.geobill.dto.ResponseData;
import com.alexistdev.geobill.utils.MessagesUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final MessagesUtils messagesUtils;

    public GlobalExceptionHandler(MessagesUtils messagesUtils) {
        this.messagesUtils = messagesUtils;
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ResponseData<Void>> handleNotFound(NotFoundException ex) {
        log.error("Not found: {}", ex.getMessage());
        return badResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseData<Void>> handleValidation(MethodArgumentNotValidException ex) {
        ResponseData<Void> response = new ResponseData<>();
        response.setStatus(false);
        ex.getBindingResult().getAllErrors()
                .forEach(err -> response.getMessages().add(err.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Body yang hilang, bukan JSON, atau tipe datanya tidak cocok. Jackson gagal sebelum
     * {@code @Valid} sempat jalan, jadi kasus ini tidak pernah sampai ke {@link #handleValidation}.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ResponseData<Void>> handleNotReadable(HttpMessageNotReadableException ex) {
        log.warn("Unreadable request body: {}", ex.getMessage());
        return badResponse(HttpStatus.BAD_REQUEST, describeUnreadableBody(ex));
    }

    /** Constraint pada parameter controller, misalnya {@code @PositiveOrZero int page}. */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ResponseData<Void>> handleParameterValidation(HandlerMethodValidationException ex) {
        ResponseData<Void> response = new ResponseData<>();
        response.setStatus(false);
        ex.getAllValidationResults().forEach(result -> result.getResolvableErrors()
                .forEach(err -> response.getMessages().add(err.getDefaultMessage())));
        if (response.getMessages().isEmpty()) {
            response.getMessages().add(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ResponseData<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        ResponseData<Void> response = new ResponseData<>();
        response.setStatus(false);
        ex.getConstraintViolations()
                .forEach(violation -> response.getMessages().add(violation.getMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ResponseData<Void>> handleMissingParameter(MissingServletRequestParameterException ex) {
        return badResponse(HttpStatus.BAD_REQUEST,
                messagesUtils.getMessage("globalexception.parameter_missing", ex.getParameterName()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ResponseData<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return badResponse(HttpStatus.BAD_REQUEST,
                messagesUtils.getMessage("globalexception.parameter_invalid", ex.getName()));
    }

    @ExceptionHandler(DuplicateException.class)
    public ResponseEntity<ResponseData<Void>> handleDuplicate(DuplicateException ex) {
        log.error("Duplicate: {}", ex.getMessage());
        return badResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(EmailExistException.class)
    public ResponseEntity<ResponseData<Void>> handleEmailExist(EmailExistException ex) {
        log.error("Email already registered: {}", ex.getMessage());
        return badResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(SuspendedException.class)
    public ResponseEntity<ResponseData<Void>> handleSuspended(SuspendedException ex) {
        log.error("Suspended: {}", ex.getMessage());
        return badResponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ResponseData<Void>> handleConflict(ConflictException ex) {
        log.error("Conflict: {}", ex.getMessage());
        return badResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    /**
     * Jaring terakhir. Detail teknisnya cukup di log, klien hanya menerima pesan umum supaya
     * struktur internal aplikasi tidak bocor lewat response.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseData<Void>> handleGeneral(Exception ex) {
        log.error("Unexpected error", ex);
        return badResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                messagesUtils.getMessage("globalexception.unexpected"));
    }

    private String describeUnreadableBody(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();

        if (cause instanceof MismatchedInputException mismatched && !mismatched.getPath().isEmpty()) {
            return messagesUtils.getMessage("globalexception.body_invalid_field", fieldPath(mismatched));
        }
        if (cause instanceof JsonProcessingException) {
            return messagesUtils.getMessage("globalexception.body_malformed");
        }
        return messagesUtils.getMessage("globalexception.body_required");
    }

    private String fieldPath(JsonMappingException ex) {
        return ex.getPath().stream()
                .map(reference -> reference.getFieldName() == null
                        ? "[" + reference.getIndex() + "]"
                        : reference.getFieldName())
                .collect(Collectors.joining("."));
    }

    private ResponseEntity<ResponseData<Void>> badResponse(HttpStatus status, String message) {
        ResponseData<Void> response = new ResponseData<>();
        response.setStatus(false);
        response.getMessages().add(message);
        return ResponseEntity.status(status).body(response);
    }
}
