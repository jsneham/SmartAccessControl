package com.smart.access.control.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.smart.access.control.R;
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
        startActivity(new Intent(this, HomeActivity.class));
    }


    public void onRegisterClick(View view) {
        startActivity(new Intent(this, RegisterActivity.class));

    }
}