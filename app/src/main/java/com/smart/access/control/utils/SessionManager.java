package com.smart.access.control.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.smart.access.control.activities.LoginActivity;

import java.util.HashMap;

public class SessionManager {
    SharedPreferences pref;

    SharedPreferences.Editor editor;

    Context _context;

    int PRIVATE_MODE = 0;

    private static final String PREF_NAME = "MegaPref";

    private static final String IS_LOGIN = "IsLoggedIn";

    public static final String KEY_PASSWORD = "password";

    public static final String KEY_EMAIL = "email";
    public static final String KEY_NAME = "name";
    public static final String KEY_USER_NAME = "userName";
    public static final String KEY_USER_TYPE = "userType";
    public static final String KEY_BASE_URL = "base_url";
    public static final String KEY_LOGINID = "loginId";
    public static final String KEY_USER_ID = "user_id";

    public static final String TOKEN = "token";


    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void createLoginSession(String email, String password, String user_id) {//String password,
        // Storing login value as TRUE
        editor.putBoolean(IS_LOGIN, true);

        // Storing name in pref
        //editor.putString(KEY_PASSWORD, password);

        // Storing email in pref
        editor.putString(KEY_LOGINID, email);
        editor.putString(KEY_USER_ID, user_id);
        editor.putString(KEY_PASSWORD, password);

        // commit changes
        editor.commit();
    }

    public void createLoginSession(String user_id){//String password,
        // Storing login value as TRUE
        editor.putBoolean(IS_LOGIN, true);

        // Storing name in pref
        //editor.putString(KEY_PASSWORD, password);

        // Storing email in pref
        editor.putString(KEY_USER_ID, user_id);

        // commit changes
        editor.commit();
    }

    public void checkLogin() {
        // Check login status
        if (!this.isLoggedIn()) {
            // user is not logged in redirect him to Login Activity
            Intent i = new Intent(_context, LoginActivity.class);
            // Closing all the activities
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Add new Flag to start new Activity
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Staring Login Activity
            _context.startActivity(i);
        }

    }

    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        // user name
        user.put(KEY_PASSWORD, pref.getString(KEY_PASSWORD, null));

        // user email id
        user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, null));

        // return user
        return user;
    }

    public String getLoginData(String key) {
        return pref.getString(key, "");
    }

    public void setUserData(String key, String value) {
        editor.putString(key, value);
        editor.commit();
    }

    public String getBaseUrl() {
        return pref.getString(KEY_BASE_URL, "");
    }

    public void setBaseUrl(String key, String value) {
        editor.putString(key, value);
        editor.commit();
    }

    public void logoutUser() {
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();


        Intent i = new Intent(_context, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        _context.startActivity(i);
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGIN, false);
    }



}
