package com.nep.connectedkitchenapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
	
	@ExceptionHandler(ApplianceConflictException.class)
    public ResponseEntity<String> handleApplianceConflict(ApplianceConflictException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT) // You can choose a more suitable status
                .body(ex.getMessage()); // Return just the message
    }
}
