package com.smart.access.control.utils;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

public class Utils {
    public static void showToast(String message, Context context) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void showSnackBar(String message, View view) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);

        // Get the Snackbar's view
        View snackbarView = snackbar.getView();

        // Change the layout parameters to position the Snackbar at the top with margins
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) snackbarView.getLayoutParams();
        params.gravity = Gravity.BOTTOM;
        params.setMargins(16, 16, 16, 16);  // Add margins to all sides (in pixels)
        snackbarView.setLayoutParams(params);

        // Set the background color to red
        snackbarView.setBackgroundColor(Color.RED);

        snackbar.show();
    }

    public static void showSuccessSnackBar(String message, View view) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);

        // Get the Snackbar's view
        View snackbarView = snackbar.getView();

        // Change the layout parameters to position the Snackbar at the top with margins
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) snackbarView.getLayoutParams();
        params.gravity = Gravity.BOTTOM;
        params.setMargins(16, 16, 16, 16);  // Add margins to all sides (in pixels)
        snackbarView.setLayoutParams(params);

        // Set the background color to red
        snackbarView.setBackgroundColor(Color.DKGRAY);

        snackbar.show();
    }
}

