package com.smart.access.control.modals;

import com.google.gson.annotations.SerializedName;

public class UserDetailsResponse {

    @SerializedName("data")
    private UserData data;

    public UserData getData() {
        return data;
    }

    public void setData(UserData data) {
        this.data = data;
    }
}



