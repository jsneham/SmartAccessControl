package com.smart.access.control.utils;


import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class InputValidation {

    private final Context context;

    public InputValidation(Context context) {
        this.context = context;
    }

    public boolean isInputEditTextFilled(TextInputEditText textInputEditText, TextInputLayout textInputLayout, String message) {
        String value = textInputEditText.getText().toString().trim();
        if (value.isEmpty()) {
            textInputLayout.setError(message);
            hideKeyboardFrom(textInputEditText);
            return false;
        } else {
            textInputLayout.setErrorEnabled(false);
        }
        return true;
    }

    public boolean isInputEditTextFilled(EditText editText, String message) {
        String value = editText.getText().toString().trim();
        if (value.isEmpty()) {
            editText.setError(message);
            hideKeyboardFrom(editText);
            return false;
        } else {
            // editText.setError(null);
        }
        return true;
    }

    public boolean isInputEditTextEmail(TextInputEditText textInputEditText, TextInputLayout textInputLayout, String message) {
        String value = textInputEditText.getText().toString().trim();
        if (value.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(value).matches()) {
            textInputLayout.setError(message);
            hideKeyboardFrom(textInputEditText);
            return false;
        } else {
            textInputLayout.setErrorEnabled(false);
        }
        return true;
    }

    public boolean isInputEditTextEmail(EditText editText, String message) {
        String value = editText.getText().toString().trim();
        if (value.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(value).matches()) {
            editText.setError(message);
            hideKeyboardFrom(editText);
            return false;
        } else {
            // editText.setError(null);
        }
        return true;
    }

    public boolean isInputEditTextLength(TextInputEditText textInputEditText, TextInputLayout textInputLayout, String message, int length) {
        String value = textInputEditText.getText().toString().trim();
        if (value.isEmpty() || value.length() < length) {
            textInputLayout.setError(message);
            hideKeyboardFrom(textInputEditText);
            return false;
        } else {
            textInputLayout.setErrorEnabled(false);
        }
        return true;
    }

    public boolean isInputEditTextLength(EditText editText, String message, int length) {
        String value = editText.getText().toString().trim();
        if (value.isEmpty() || value.length() < length) {
            editText.setError(message);
            hideKeyboardFrom(editText);
            return false;
        } else {
            // editText.setError(null);
        }
        return true;
    }

    public boolean isInputEditTextMatches(TextInputEditText textInputEditText1, TextInputEditText textInputEditText2, String message) {
        String value1 = textInputEditText1.getText().toString().trim();
        String value2 = textInputEditText2.getText().toString().trim();
        if (!value1.contentEquals(value2)) {
            textInputEditText1.setError(message);
            textInputEditText2.setError(message);
            hideKeyboardFrom(textInputEditText2);
            return false;
        } else {
            // textInputLayout.setErrorEnabled(false);
        }
        return true;
    }

    private void hideKeyboardFrom(View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public boolean isInputEditTextsFilled(TextInputEditText textInputEditText1, TextInputEditText textInputEditText2, TextInputLayout textInputLayout1, TextInputLayout textInputLayout2, String message, String error) {
        String value1 = textInputEditText1.getText().toString().trim();
        String value2 = textInputEditText2.getText().toString().trim();
        if (value1.isEmpty() && value2.isEmpty()) {
            textInputLayout1.setError(message);
            hideKeyboardFrom(textInputEditText1);
            hideKeyboardFrom(textInputEditText2);
            return false;
        } else if (!value1.isEmpty()) {
            int length = value1.length();
            if (length < 10) {
                textInputLayout1.setError(error);
                return false;
            }
        } else if (!value2.isEmpty()) {
            int length = value2.length();
            if (length < 10) {
                textInputLayout2.setError(error);
                return false;
            }
        } else {
            textInputLayout1.setErrorEnabled(false);
            textInputLayout2.setErrorEnabled(false);
        }
        return true;
    }
}
