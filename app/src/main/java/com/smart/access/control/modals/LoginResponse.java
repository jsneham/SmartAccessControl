package com.smart.access.control.modals;

public class LoginResponse {
    private String token;
    private String message;

    // Constructor
    public LoginResponse() {
    }

    public LoginResponse(String token) {
        this.token = token;
    }

    // Getter
    public String getToken() {
        return token;
    }

    public String getMessage() {
        return message;
    }

    // Setter
    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "LoginResponse{" +
                "token='" + token + '\'' +
                '}';
    }
}
