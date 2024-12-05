package com.sparta.sportify.util.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sparta.sportify.dto.user.res.SignupResponseDto;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@JsonPropertyOrder({"success", "data", "apiError", "timestamp"})
public class ApiResult<T> {

    private final ApiError apiError;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private final LocalDateTime timestamp = LocalDateTime.now();
    private final boolean success;
    private final T data;

    public ApiResult(boolean success, T data, ApiError apiError) {
        this.apiError = apiError;
        this.success = success;
        this.data = data;
    }

    public static <T> ApiResult<T> success(String message,T data) {
        return new ApiResult<>(true, data, null);
    }

    public static <T> ApiResult<T> error(int status, String msg) {
        return new ApiResult<>(false, null, new ApiError(msg, status));
    }

    public static <T> ApiResult<T> error(int status, String msg, T errorMap) {
        return new ApiResult<>(false, errorMap, new ApiError(msg, status));
    }

    public static <T> ApiResult<T> failure(String message, T data) {
        return new ApiResult<>(false, data, new ApiError(message, HttpStatus.FORBIDDEN.value()));
    }
}
