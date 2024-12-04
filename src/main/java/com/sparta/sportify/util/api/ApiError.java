package com.sparta.sportify.util.api;

import lombok.Getter;

@Getter
public class ApiError {
    private final String msg;
    private final int status;

    public ApiError(String msg, int status) {
        this.msg = msg;
        this.status = status;
    }
}
