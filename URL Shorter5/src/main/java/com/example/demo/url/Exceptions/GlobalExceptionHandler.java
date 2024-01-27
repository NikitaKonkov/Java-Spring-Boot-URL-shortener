package com.example.demo.url.Exceptions;

import com.example.demo.url.Exceptions.WarningException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)    // NOT FOUND
    public ResponseEntity<Object> IllegalArgumentException(EntityNotFoundException e) {
        Map<String, Object> body = new HashMap<>();
        body.put("DATE", new Date());
        body.put("MSG", e.getMessage());
        body.put("STATUS", 400);
        body.put("ERROR", "DATA NOT FOUND");
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> IllegalArgumentException(IllegalArgumentException e) {
        Map<String, Object> body = new HashMap<>();
        body.put("DATE", new Date());
        body.put("MSG", e.getMessage());
        body.put("STATUS", 406);
        body.put("ERROR", "ILLEGAL ARGUMENT");
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(UrlAlreadyRegisteredException.class)
    public ResponseEntity<Object> handleUrlAlreadyRegisteredException(UrlAlreadyRegisteredException e) {
        Map<String, Object> body = new HashMap<>();
        body.put("DATE", new Date());
        body.put("MSG", e.getMessage());
        body.put("STATUS", 400);
        body.put("ERROR", "URL ALREADY REGISTERED");
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(IdAlreadyRegisteredException.class)
    public ResponseEntity<Object> IdAlreadyRegisteredException(IdAlreadyRegisteredException e) {
        Map<String, Object> body = new HashMap<>();
        body.put("DATE", new Date());
        body.put("MSG", e.getMessage());
        body.put("STATUS", 400);
        body.put("ERROR", "ID ALREADY REGISTERED");
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(InvalidUrlException.class)
    public ResponseEntity<Object> InvalidUrlException(InvalidUrlException e) {
        Map<String, Object> body = new HashMap<>();
        body.put("DATE", new Date());
        body.put("MSG", e.getMessage());
        body.put("STATUS", 400);
        body.put("ERROR", "INVALID URL");
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(WrongTtlException.class)
    public ResponseEntity<Object> WrongTtlException(WrongTtlException e) {
        Map<String, Object> body = new HashMap<>();
        body.put("DATE", new Date());
        body.put("MSG", e.getMessage());
        body.put("STATUS", 400);
        body.put("ERROR", "INVALID TTL");
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(WarningException.class)
    public ResponseEntity<String> WarningException(WarningException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
    }
    @ExceptionHandler(WebPageException.class)
    public ResponseEntity<String> WebPageException(WebPageException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.ACCEPTED);
    }
}
