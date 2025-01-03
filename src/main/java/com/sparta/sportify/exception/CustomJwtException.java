package com.sparta.sportify.exception;

import lombok.Getter;

@Getter
public class CustomJwtException extends RuntimeException{

    int status;
    String msg;

    public CustomJwtException(int status, String msg) {
        super(msg);
        this.status = status;
        this.msg = msg;
    }
}
