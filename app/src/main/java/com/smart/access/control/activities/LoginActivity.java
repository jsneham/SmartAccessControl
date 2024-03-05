package com.smart.access.control.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.smart.access.control.R;
import com.smart.access.control.retrofit.ServiceGenerator;
import com.smart.access.control.utils.InputValidation;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private InputValidation inputValidation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        etEmail=findViewById(R.id.etEmail);
        etPassword=findViewById(R.id.etPassword);
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

//            Call<String> call = ServiceGenerator.apiService.loginClient(etEmail.getText().toString(), etPassword.getText().toString());
//            call.enqueue(new Callback<LoginResponse>() {
//                @Override
//                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
//
//                    if (response.isSuccessful()) {
//                        if (response.body().getResponse().equals("200")) {
//                            LoginData data= response.body().getLoginData();
//                            if(data!=null) {
//                                sessionManager.setUserData(SessionManager.user_logged_id, data.getUserLoggedId());
//                                sessionManager.setUserData(SessionManager.user_email, data.getUserEmail());
//                                sessionManager.setUserData(SessionManager.user_mobile, data.getUserMobile());
//                                sessionManager.setUserData(SessionManager.user_full_name, data.getUserFullName());
//                                sessionManager.setUserData(SessionManager.user_type, data.getUserType());
//                                sessionManager.setUserData(SessionManager.user_birth, data.getUserBirth());
//                                sessionManager.setUserData(SessionManager.user_image, image_Url+ data.getUserImage());
//                                sessionManager.setUserData(SessionManager.user_gender, data.getUserGender());
//                                sessionManager.setUserData(SessionManager.IS_LOGIN, true);
//                                startHomeActivity();
//                            }
//                            else {
//                                Toast.makeText(context, response.body().getMessage(), Toast.LENGTH_SHORT).show();
//                            }
//
//                        }
//                        else {
//                            Toast.makeText(context, response.body().getMessage(), Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                    else {
//                        showToast(getString(R.string.something_wrong));
//                    }
//
//                }
//
//                @Override
//                public void onFailure(Call<LoginResponse> call, Throwable t) {
//                    call.cancel();
//                    showToast(getString(R.string.something_wrong));
//                }
//            });

        } catch (Exception e) {
            e.printStackTrace();
//            showToast(getString(R.string.something_wrong));
        }

        startActivity(new Intent(this, HomeActivity.class));
    }


    public void onRegisterClick(View view) {
        startActivity(new Intent(this, RegisterActivity.class));

    }
}