package com.sparta.sportify.config;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;

@Getter
@JsonPropertyOrder({"statusCode", "message", "data"})
public class ResponseBodyDto<T> {
    private final String statusCode;
    private final String message;
    private final T data;

    public ResponseBodyDto(String statusCode, String message, T data) {
        this.statusCode = statusCode;
        this.message = message;
        this.data = data;
    }

    public static <T> ResponseBodyDto<T> success(String message,T data) {
        return new ResponseBodyDto<>("Success", message ,data);
    }
    public static <T> ResponseBodyDto<T> success(String message) {
        return new ResponseBodyDto<>("Success", message ,null);
    }

}
