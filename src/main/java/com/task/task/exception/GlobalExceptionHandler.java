
package com.task.task.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Object> handleValidationErrors(ResponseStatusException ex) {
        try {
            // message already contains JSON errors string → convert to Map
            Map<String, Object> errors = objectMapper.readValue(ex.getReason(), Map.class);

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "success", false,
                            "errors", errors
                    ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "success", false,
                            "errors", Map.of("unknown", "Invalid request")
                    ));
        }
    }
}
