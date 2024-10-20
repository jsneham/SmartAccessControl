package com.smart.access.control.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;

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

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private RelativeLayout llRoot;
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
        finish();
        startActivity(new Intent(this, HomeActivity.class));
    }

    private void initView() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        llRoot = findViewById(R.id.llRoot);
    }

    public void onLoginClick(View view) {
        hideKeyboard();
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

    public void onPasswordClick(View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Forget Password");
        builder.setMessage("Forgot your password? Need help? choose option below and get help.");

        builder.setPositiveButton("Whatsapp", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String phoneNumber = context.getString(R.string.phone_number_help);
                Uri uri = Uri.parse(context.getString(R.string.whatsapp) + phoneNumber);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                context.startActivity(intent);
            }
        });
        builder.setNegativeButton("Email", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                sendMail();
            }
        });

// Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();


    }

    private void sendMail() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:")); // Only email apps should handle this
//        emailIntent.setType("message/rfc822"); // Ensure only email apps are shown
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"abmtllp@gmail.com"}); // Replace with the recipient's email
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Forgot password : Smart Access Control");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Please add your contact details");
        startActivity(emailIntent);

    }

    private void loginClient() {

        try {
            ApplicationInterface apiService = ServiceGenerator.apiService;

            Call<LoginResponse> call = apiService.loginClient(etEmail.getText().toString(), etPassword.getText().toString());
            call.enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> apiResponse) {
                    if (apiResponse.isSuccessful()) {
                        if (apiResponse.body() != null) {
                            LoginResponse data = apiResponse.body();
                            String token = data.getToken();
                            String successMessage = data.getMessage();

                            sessionManager.setUserData(SessionManager.TOKEN, token);
                            Utils.showSuccessSnackBar(successMessage, llRoot);
                            getUserInformationCall(token, etPassword.getText().toString());
                        } else {
                            Utils.showSnackBar(apiResponse.message(), llRoot);
                        }
                    } else {
                        // Handle wrong credentials or other errors
                        try {
                            String errorResponse = apiResponse.errorBody().string();
                            JSONObject errorObject = new JSONObject(errorResponse);
                            String errorMessage = errorObject.getString("message");
                            Utils.showSnackBar(errorMessage, llRoot);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Utils.showSnackBar(getString(R.string.something_wrong), llRoot);
                        }
                    }
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    t.printStackTrace();
                    Utils.showSnackBar(t.getMessage(), llRoot);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Utils.showSnackBar(getString(R.string.something_wrong), llRoot);
        }
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
                            UserData data = apiResponse.getData();
                            sessionManager.createLoginSession(data.getEmail(), password, data.getId());
                            setData(data);


                        } else {
                            Utils.showSnackBar(getString(R.string.something_wrong), llRoot);
                        }
                    } else {
                        Utils.showSnackBar(getString(R.string.something_wrong), llRoot);
                    }
                }

                @Override
                public void onFailure(Call<UserDetailsResponse> call, Throwable t) {
                    t.printStackTrace();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Utils.showSnackBar(getString(R.string.something_wrong), llRoot);
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