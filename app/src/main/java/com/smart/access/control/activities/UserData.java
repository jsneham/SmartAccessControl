package com.smart.access.control.activities;

import java.util.Arrays;

public class UserData {
    private byte[] userName;
    private byte[] userId;

    // Default constructor
    public UserData() {
    }

    // Parameterized constructor
    public UserData(byte[] userName, byte[] userId) {
        this.userName = userName;
        this.userId = userId;
    }

    public UserData(byte[] userId) {
        this.userId = userId;
    }

    // Getters and setters
    public byte[] getUserName() {
        return userName;
    }

    public void setUserName(byte[] userName) {
        this.userName = userName;
    }

    public byte[] getUserId() {
        return userId;
    }

    public void setUserId(byte[] userId) {
        this.userId = userId;
    }

    public String getUserIdAsString() {
        return Arrays.toString(userId);
    }

}

