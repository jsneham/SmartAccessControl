package com.smart.access.control.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smart.access.control.R;

public class SettingActivity extends AppCompatActivity {

    String youtube= "https://www.youtube.com/@abmt_llp";
    String linkedIn= "https://www.linkedin.com/in/abmtllp/";
    String facebook ="https://www.facebook.com/share/wK2FwrLWzK6e6S94/?mibextid=qi2Omg";
    String insta ="https://www.instagram.com/abmt_llp";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        LinearLayout llAbout= findViewById(R.id.llAbout);
        llAbout.setVisibility(View.GONE);
        ImageView iv1= findViewById(R.id.iv1);
        ImageView iv2= findViewById(R.id.iv2);
        ImageView iv3= findViewById(R.id.iv3);
        ImageView iv4= findViewById(R.id.iv4);

        iv1.setOnClickListener(view -> {
            startIntent(youtube);
        });
        iv2.setOnClickListener(view -> {
            startIntent(linkedIn);
        });
        iv3.setOnClickListener(view -> {
            startIntent(facebook);
        });
        iv4.setOnClickListener(view -> {
            startIntent(insta);
        });

        String tag = getIntent().getStringExtra("tag");
        TextView tvDetails= findViewById(R.id.tvDetails);
        switch (tag) {
            case "FAQs":
                getSupportActionBar().setTitle(tag);
                tvDetails.setText(getString(R.string.faq));
                break;
            case "About Us":
                llAbout.setVisibility(View.VISIBLE);
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

    private void startIntent(String url) {
        // Create intent to open the URL
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));

        // Verify there is an activity to handle the intent
            try {
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("startIntent", "Error starting intent: " + e.getMessage());
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