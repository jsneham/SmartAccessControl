package com.smart.access.control.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private InputValidation inputValidation;
    private Context context = this;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sessionManager = new SessionManager(context);
        if (sessionManager.isLoggedIn()) {
            goToHome();
        } else {
            initView();
        }
    }

    private void goToHome() {
        startActivity(new Intent(this, HomeActivity.class));
    }

    private void initView() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
    }

    public void onLoginClick(View view) {
        inputValidation = new InputValidation(this);

        if (!inputValidation.isInputEditTextFilled(etEmail, getString(R.string.invalid_username))) {
            return;
        } else if (!inputValidation.isInputEditTextEmail(etEmail, getString(R.string.invalid_username))) {
            return;
        } else if (!inputValidation.isInputEditTextFilled(etPassword, getString(R.string.invalid_password))) {
            return;
        } else {
            loginClient();
        }
    }

    private void loginClient() {

        try {
            ApplicationInterface apiService = ServiceGenerator.apiService;

            // Replace with actual email and password
            Call<LoginResponse> call = apiService.loginClient(etEmail.getText().toString(), etPassword.getText().toString());
            call.enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    LoginResponse apiResponse = response.body();
                    if (response.isSuccessful()) {
                        if (apiResponse != null) {
                            String token= apiResponse.getToken();
                            sessionManager.setUserData(SessionManager.TOKEN,token);
                            getUserInformationCall(token,etPassword.getText().toString());
                        } else {
                            Utils.showToast(response.message(), context);
                        }
                    } else {
                        Utils.showToast(response.message(), context);
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

    private void getUserInformationCall(String token, String password) {

        try {

            // Replace with actual email and password
            Call<UserDetailsResponse> call = ServiceGenerator.apiService.userClient(Urls.Bearer+" " +token);
            call.enqueue(new Callback<UserDetailsResponse>() {
                @Override
                public void onResponse(Call<UserDetailsResponse> call, Response<UserDetailsResponse> response) {
                    UserDetailsResponse apiResponse = response.body();
                    if (response.isSuccessful()) {
                        if (apiResponse != null) {
                            UserData data= apiResponse.getData();
                            sessionManager.createLoginSession(data.getEmail(),password, data.getId());
                                setData(data);


                        } else {
                            Utils.showToast(getString(R.string.something_wrong), context);
                        }
                    } else {
                        Utils.showToast(getString(R.string.something_wrong), context);
                    }
                }

                @Override
                public void onFailure(Call<UserDetailsResponse> call, Throwable t) {
                    t.printStackTrace();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Utils.showToast(getString(R.string.something_wrong), context);
        }
    }

    private void setData(UserData data) {
        sessionManager.setUserData(SessionManager.KEY_EMAIL, data.getEmail());
        sessionManager.setUserData(SessionManager.KEY_USER_ID, data.getId());
        sessionManager.setUserData(SessionManager.KEY_NAME, data.getName());
        sessionManager.setUserData(SessionManager.KEY_USER_NAME, data.getUsername());
        sessionManager.setUserData(SessionManager.KEY_USER_TYPE, data.getUserType());
        goToHome();
    }


    public void onRegisterClick(View view) {
        startActivity(new Intent(this, RegisterActivity.class));

    }
}