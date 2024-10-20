package com.smart.access.control.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;
import com.smart.access.control.R;
import com.smart.access.control.modals.LoginResponse;
import com.smart.access.control.modals.UserData;
import com.smart.access.control.modals.UserDetailsResponse;
import com.smart.access.control.retrofit.ApplicationInterface;
import com.smart.access.control.retrofit.ServiceGenerator;
import com.smart.access.control.retrofit.Urls;
import com.smart.access.control.utils.InputValidation;
import com.smart.access.control.utils.SessionManager;
import com.smart.access.control.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etFirstName, etLastName, etUserName, etCPassword;
    private TextView etName, etUserNameLayout, etEmailLayout, etPasswordLayout, etCPasswordLayout;
    private InputValidation inputValidation;
    private LinearLayout llOne;
    private Context context = this;
    private SessionManager sessionManager;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        sessionManager = new SessionManager(context);
        initView();
    }

    private void initView() {
        llOne = findViewById(R.id.llOne);
        etUserName = findViewById(R.id.etUserName);
        etLastName = findViewById(R.id.etLastName);
        etFirstName = findViewById(R.id.etFirstName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etCPassword = findViewById(R.id.etCPassword);

        etName = findViewById(R.id.etName);
//        etLastNameLayout = findViewById(R.id.etLastNameLayout);
        etUserNameLayout = findViewById(R.id.etUserNameLayout);
        etEmailLayout = findViewById(R.id.etEmailLayout);
        etPasswordLayout = findViewById(R.id.etPasswordLayout);
        etCPasswordLayout = findViewById(R.id.etCPasswordLayout);
    }

    public void goToLogin() {
        startActivity(new Intent(this, LoginActivity.class));

    }

    public void onRegisterClick(View view) {
        hideKeyboard();
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String userName = etUserName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String cPassword = etCPassword.getText().toString().trim();
        boolean isValid = true;


        // Check if first name is not empty
        if (firstName.isEmpty()) {
            Utils.showSnackBar("Please enter your first name", llOne);
            etFirstName.setError("Please enter your first name");
            // isValid = false;
            return;
        }
//        else {
//            etName.setText("");
//        }

        // Check if last name is not empty
        if (lastName.isEmpty()) {
            Utils.showSnackBar("Please enter your last name", llOne);
            etLastName.setError("Please enter your last name");
            // isValid = false;

            return;
        }
//        else {
//            etName.setText("");
//        }


        // Check if username is valid
        if (userName.isEmpty()) {
            Utils.showSnackBar("Please enter a username", llOne);
            etUserName.setError("Please enter a username");
            // isValid = false;

            return;
        } else if (userName.length() < 6 || userName.length() > 20) {
            Utils.showSnackBar("Username must be between 6 and 20 characters", llOne);
            etUserName.setError("Username must be between 6 and 20 characters");
            return;
        }
//        else if (!userName.matches("^(?=.*[a-zA-Z]{3})(?=.*\\d{2})(?=.*[^a-zA-Z\\d]).{6}$")) {
//            Utils.showSnackBar("Username must be 6 characters: 3 letters, 2 digits, 1 special character", llOne);
//            etUserName.setError("Username must be 6 characters: 3 letters, 2 digits, 1 special character");
//            // isValid = false;
//
//            return;
//        }
//        else {
//            etUserNameLayout.setText("");
//        }

        // Check if email is not empty and is in valid format
        if (email.isEmpty()) {
            Utils.showSnackBar("Please enter your email address", llOne);
            etEmail.setError("Please enter your email address");
            // isValid = false;

            return;
        } else if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.(com|in|co\\.in)$")) {
            Utils.showSnackBar("Please enter a valid email address", llOne);
            etEmail.setError("Please enter a valid email address");
            // isValid = false;

            return;
        } else if (password.equals(email)) {
            Utils.showSnackBar("Email and password should not be the same", llOne);
            etEmail.setError("Email and password should not be the same");
            // isValid = false;

            return;
        }
//        else {
//            etEmailLayout.setText("");
//        }

        // Check if password is not empty and meets criteria
        if (password.isEmpty()) {
            Utils.showSnackBar("Please enter a password", llOne);
            etPassword.setError("Please enter a password");
            // isValid = false;

            return;
        }
//        else if (!password.matches("^(?=.*[a-zA-Z]{4})(?=.*\\d{3})(?=.*[^a-zA-Z\\d]).{8}$")) {
        else if (password.length() != 8) {
            Utils.showSnackBar("Password must be 8 characters", llOne);
            etPassword.setError("Password must be 8 characters");
            // isValid = false;


            return;
        } else if (password.equals(userName)) {
            Utils.showSnackBar("Password should not be the same as username", llOne);
            etPassword.setError("Password should not be the same as username");
            // isValid = false;


            return;
        }

        // Check if confirm password is not empty and matches the password
        if (cPassword.isEmpty()) {
            Utils.showSnackBar("Please confirm your password", llOne);
            etCPassword.setError("Please confirm your password");
            // isValid = false;

            return;
        } else if (!cPassword.equals(password)) {
            Utils.showSnackBar("Passwords do not match", llOne);
            etCPassword.setError("Passwords do not match");
            // isValid = false;

            return;
        }


        // If all validation checks pass, proceed with further actions (e.g., registration)
        registerClient();
//        if (isValid) {
//            registerClient();
//        }
    }

    private void hideKeyboard() {

        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            View view = getCurrentFocus();
            if (view != null) {
                if (imm.isAcceptingText()) {
                    // The keyboard is open, so hide it
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

    private void registerClient() {
        try {
            showProgressDialog();
            ApplicationInterface apiService = ServiceGenerator.apiService;
            String name = etFirstName.getText().toString() + " " + etLastName.getText().toString();
            String userName = etUserName.getText().toString();
            String email = etEmail.getText().toString();
            String password = etPassword.getText().toString();
            String cPassword = etCPassword.getText().toString();
            // Replace with actual email and password
            Call<LoginResponse> call = apiService.registerClient(name, userName, "1", email, password, cPassword);
            call.enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> apiResponse) {
//                    LoginResponse apiResponse = response.body();
//                    if (response.isSuccessful() && apiResponse != null) {
//                        Utils.showToast(apiResponse.getMessage(), context);
//                        goToLogin();
//                    } else {
//                        Utils.showToast(getString(R.string.registration_unsuccessful), context);
//                    }

                    if (apiResponse.isSuccessful()) {
                        if (apiResponse.body() != null) {
                            LoginResponse data = apiResponse.body();
                            String token = data.getToken();
                            String successMessage = data.getMessage();

                            sessionManager.setUserData(SessionManager.TOKEN, token);
                            Utils.showSuccessSnackBar(successMessage, llOne);
                            getUserInformationCall(token, etPassword.getText().toString());
                        } else {
                            Utils.showSnackBar(apiResponse.message(), llOne);
                        }
                    } else {
                        // Handle wrong credentials or other errors
                        try {
                            hideProgressDialog();
                            String errorResponse = apiResponse.errorBody().string();
                            JSONObject errorObject = new JSONObject(errorResponse);
//                            String errorMessage = errorObject.getString("message");
                            Utils.showSnackBar(setError(errorObject), llOne);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Utils.showSnackBar(getString(R.string.something_wrong), llOne);
                        }
                    }
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    t.printStackTrace();
                    hideProgressDialog();
                    Utils.showToast(getString(R.string.something_wrong), context);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            hideProgressDialog();
            Utils.showToast(getString(R.string.something_wrong), context);
        }
    }

    private String setError(JSONObject errorObject) {
        try {
            JSONObject messageObject = errorObject.getJSONObject("message");

            // StringBuilder to collect all messages
            StringBuilder allMessages = new StringBuilder();

            // Iterate through the keys in the "message" object
            Iterator<String> keys = messageObject.keys();  // Use keys() instead of keySet()
            while (keys.hasNext()) {
                String key = keys.next();
                JSONArray messagesArray = messageObject.getJSONArray(key);
                for (int i = 0; i < messagesArray.length(); i++) {
                    allMessages.append(messagesArray.getString(i)).append(" ");
                }
            }

            // Remove the trailing space
            String result = allMessages.toString().trim();
            return result;
        } catch (JSONException e) {
            return "";
        }
    }


    private void showProgressDialog() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Registering user in AMBT System.");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false); // Prevent dismissing by tapping outside
        progressDialog.show();
    }

    private void hideProgressDialog() {
        progressDialog.dismiss();
    }


    public void onLoginClick(View view) {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    public void onBack(View view) {
        onBackPressed();
    }

    private void getUserInformationCall(String token, String password) {

        try {

            // Replace with actual email and password
            Call<UserDetailsResponse> call = ServiceGenerator.apiService.userClient(Urls.Bearer + " " + token);
            call.enqueue(new Callback<UserDetailsResponse>() {
                @Override
                public void onResponse(Call<UserDetailsResponse> call, Response<UserDetailsResponse> response) {
                    UserDetailsResponse apiResponse = response.body();
                    if (response.isSuccessful()) {
                        if (apiResponse != null) {
                            com.smart.access.control.modals.UserData data = apiResponse.getData();
                            sessionManager.createLoginSession(data.getEmail(), password, data.getId());
                            setData(data);


                        } else {
                            hideProgressDialog();
                            Utils.showSnackBar(getString(R.string.something_wrong), llOne);
                        }
                    } else {
                        hideProgressDialog();
                        Utils.showSnackBar(getString(R.string.something_wrong), llOne);
                    }
                }

                @Override
                public void onFailure(Call<UserDetailsResponse> call, Throwable t) {
                    hideProgressDialog();
                    t.printStackTrace();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Utils.showSnackBar(getString(R.string.something_wrong), llOne);
        }
    }

    private void setData(UserData data) {
        sessionManager.setUserData(SessionManager.KEY_EMAIL, data.getEmail());
        sessionManager.setUserData(SessionManager.KEY_USER_ID, data.getId());
        sessionManager.setUserData(SessionManager.KEY_NAME, data.getName());
        sessionManager.setUserData(SessionManager.KEY_USER_NAME, data.getUsername());
        sessionManager.setUserData(SessionManager.KEY_USER_TYPE, data.getUserType());
        hideProgressDialog();
        goToHome();
    }

    private void goToHome() {
        finish();
        startActivity(new Intent(this, HomeActivity.class));
    }
}