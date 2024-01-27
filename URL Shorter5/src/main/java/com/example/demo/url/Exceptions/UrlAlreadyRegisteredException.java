package com.example.demo.url.Exceptions;

public class UrlAlreadyRegisteredException extends RuntimeException {

    public UrlAlreadyRegisteredException(String message) {
        super(message);
    }
}
