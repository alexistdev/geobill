package com.alexistdev.geobill.exceptions;

import com.alexistdev.geobill.dto.ResponseData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ResponseData<Void>> handleNotFound(NotFoundException ex) {
        log.error("Not found: {}", ex.getMessage());
        ResponseData<Void> response = new ResponseData<>();
        response.setStatus(false);
        response.getMessages().add(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseData<Void>> handleValidation(MethodArgumentNotValidException ex) {
        ResponseData<Void> response = new ResponseData<>();
        response.setStatus(false);
        ex.getBindingResult().getAllErrors()
                .forEach(err -> response.getMessages().add(err.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseData<Void>> handleGeneral(Exception ex) {
        log.error("Unexpected error", ex);
        ResponseData<Void> response = new ResponseData<>();
        response.setStatus(false);
        response.getMessages().add("Error: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ResponseData<Void>> handleConflict(ConflictException ex) {
        log.error("Conflict: {}", ex.getMessage());
        ResponseData<Void> response = new ResponseData<>();
        response.setStatus(false);
        response.getMessages().add(ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
}
