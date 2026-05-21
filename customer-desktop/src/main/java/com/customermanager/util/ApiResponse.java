package com.customermanager.util;

/**
 * Generic wrapper that carries either a successful result or an error message
 * from every HTTP call.
 */
public class ApiResponse<T> {

    private final T      data;
    private final String errorMessage;
    private final int    statusCode;
    private final boolean success;

    private ApiResponse(T data, String errorMessage, int statusCode, boolean success) {
        this.data         = data;
        this.errorMessage = errorMessage;
        this.statusCode   = statusCode;
        this.success      = success;
    }

    public static <T> ApiResponse<T> success(T data, int statusCode) {
        return new ApiResponse<>(data, null, statusCode, true);
    }

    public static <T> ApiResponse<T> failure(String errorMessage, int statusCode) {
        return new ApiResponse<>(null, errorMessage, statusCode, false);
    }

    public T       getData()         { return data; }
    public String  getErrorMessage() { return errorMessage; }
    public int     getStatusCode()   { return statusCode; }
    public boolean isSuccess()       { return success; }
}
