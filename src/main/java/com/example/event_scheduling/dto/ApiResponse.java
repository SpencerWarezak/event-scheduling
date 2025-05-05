package com.example.event_scheduling.dto;

public class ApiResponse<T> {
    public String message;
    public T data;

    public ApiResponse(String message, T data) {
        this.message = message;
        this.data = data;
    }

    public ApiResponse() {
        this.message = "Empty response object";
        this.data = null;
    }
}
