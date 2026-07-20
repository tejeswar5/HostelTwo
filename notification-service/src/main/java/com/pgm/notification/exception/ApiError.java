package com.pgm.notification.exception;

import java.util.List;

public record ApiError(int status, String error, String message, List<String> details) {
    public static ApiError of(int status, String error, String message) {
        return new ApiError(status, error, message, null);
    }
}
