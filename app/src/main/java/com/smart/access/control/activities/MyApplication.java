package com.smart.access.control.activities;


import android.app.Activity;
import android.app.Application;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatDelegate;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        // Initialize any necessary libraries or services here
    }

    @Override
    public void registerActivityLifecycleCallbacks(ActivityLifecycleCallbacks callback) {
        super.registerActivityLifecycleCallbacks(callback);
        // Add any additional code that you want to execute when an activity is created or destroyed
    }

    private final ActivityLifecycleCallbacks activityLifecycleCallbacks = new ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            // Handle activity created event
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        @Override
        public void onActivityStarted(Activity activity) {
            // Handle activity started event
        }

        @Override
        public void onActivityResumed(Activity activity) {
            // Handle activity resumed event
        }

        @Override
        public void onActivityPaused(Activity activity) {
            // Handle activity paused event
        }

        @Override
        public void onActivityStopped(Activity activity) {
            // Handle activity stopped event
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            // Handle activity save instance state event
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            // Handle activity destroyed event
        }
    };

    // Add getters and setters for spinData if needed
}

