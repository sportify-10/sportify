package com.sparta.sportify.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidOAuthException extends RuntimeException {
    public InvalidOAuthException(String message) {
        super(message);
    }
}

