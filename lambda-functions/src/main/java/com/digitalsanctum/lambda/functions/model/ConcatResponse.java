package com.digitalsanctum.lambda.functions.model;

public class ConcatResponse {

    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String toString() {
        return "TestResponse{" +
                "message='" + message + '\'' +
                '}';
    }
}
