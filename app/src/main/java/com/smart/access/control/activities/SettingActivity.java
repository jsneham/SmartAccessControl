package com.smart.access.control.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.smart.access.control.R;

public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        

        String tag = getIntent().getStringExtra("tag");
        TextView tvDetails= findViewById(R.id.tvDetails);
        switch (tag) {
            case "FAQs":
                getSupportActionBar().setTitle(tag);
                tvDetails.setText(getString(R.string.faq));
                break;
            case "About Us":
                getSupportActionBar().setTitle(tag);
                tvDetails.setText(getString(R.string.about_us));
                break;
            case "Terms & Conditions":
                getSupportActionBar().setTitle(tag);
                tvDetails.setText(getString(R.string.term));
                break;
            case "Privacy Policy":
                getSupportActionBar().setTitle(tag);
                tvDetails.setText(getString(R.string.privacy));
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
  
}