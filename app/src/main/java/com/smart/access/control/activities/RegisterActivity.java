package com.smart.access.control.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.smart.access.control.R;
import com.smart.access.control.modals.LoginResponse;
import com.smart.access.control.retrofit.ApplicationInterface;
import com.smart.access.control.retrofit.ServiceGenerator;
import com.smart.access.control.utils.InputValidation;
import com.smart.access.control.utils.SessionManager;
import com.smart.access.control.utils.Utils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etFirstName, etLastName, etUserName, etCPassword;
    private InputValidation inputValidation;
    private Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initView();
    }

    private void initView() {
        etUserName = findViewById(R.id.etUserName);
        etLastName = findViewById(R.id.etLastName);
        etFirstName = findViewById(R.id.etFirstName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etCPassword = findViewById(R.id.etCPassword);
    }

    public void goToLogin() {
        startActivity(new Intent(this, LoginActivity.class));

    }

    public void onRegisterClick(View view) {
        String name = etFirstName.getText().toString() + etLastName.getText().toString();
        String userName = etUserName.getText().toString();
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();
        String cPassword = etCPassword.getText().toString();

// Check if name is not empty
        if (name.trim().isEmpty()) {
            // Display error message or prompt user to enter name
            etFirstName.setError("Please enter your first name");
            return;
        }

// Check if username is not empty
        if (userName.trim().isEmpty()) {
            // Display error message or prompt user to enter username
            etUserName.setError("Please enter a username");
            return;
        }

// Check if email is not empty and is in valid format
        if (email.trim().isEmpty()) {
            // Display error message or prompt user to enter email
            etEmail.setError("Please enter your email address");
            return;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            // Display error message or prompt user to enter valid email
            etEmail.setError("Please enter a valid email address");
            return;
        }

// Check if password is not empty and meets criteria (e.g., minimum length)
        if (password.trim().isEmpty()) {
            // Display error message or prompt user to enter password
            etPassword.setError("Please enter a password");
            return;
        } else if (password.length() < 6) { // Adjust minimum password length as needed
            // Display error message or prompt user to enter a longer password
            etPassword.setError("Password must be at least 6 characters long");
            return;
        }

// Check if confirm password matches the password
        if (!cPassword.equals(password)) {
            // Display error message or prompt user that passwords do not match
            etCPassword.setError("Passwords do not match");
            return;
        }

// If all validation checks pass, proceed with further actions (e.g., registration)
        registerClient();
    }

    private void registerClient() {
        try {
            ApplicationInterface apiService = ServiceGenerator.apiService;
            String name = etFirstName.getText().toString() + " " +etLastName.getText().toString();
            String userName = etUserName.getText().toString();
            String email = etEmail.getText().toString();
            String password = etPassword.getText().toString();
            String cPassword = etCPassword.getText().toString();
            // Replace with actual email and password
            Call<LoginResponse> call = apiService.registerClient(name, userName, "1", email, password, cPassword);
            call.enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    LoginResponse apiResponse = response.body();
                    if (response.isSuccessful()) {
                        if (apiResponse != null) {
                            goToLogin();

                        } else {
                            Utils.showToast(apiResponse.getMessage(), context);
                        }
                    } else {
                        Utils.showToast(apiResponse.getMessage(), context);
                    }
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    t.printStackTrace();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Utils.showToast(getString(R.string.something_wrong), context);
        }
    }


    public void onLoginClick(View view) {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    public void onBack(View view) {
        onBackPressed();
    }
}