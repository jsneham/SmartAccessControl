package com.smart.access.control.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.smart.access.control.R;
import com.smart.access.control.adapters.GridAdapter;

import java.util.ArrayList;

public class HomeGridActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    GridAdapter gridAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_grid);

        setRecyclerView();
    }

    private void setRecyclerView() {


        ArrayList<String> itemList = new ArrayList<>();
        itemList.add("User Management");
        itemList.add("Dummy");
        itemList.add("Dummy");
        itemList.add("Dummy");
        itemList.add("Dummy");
        itemList.add("Dummy");

        recyclerView = findViewById(R.id.recyclerView);
        gridAdapter = new GridAdapter(this, itemList);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(gridAdapter);

        // Set item click listener
        gridAdapter.setOnItemClickListener(new GridAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if(position==0) {
                    openMasterKeyPopUp();
                }
            }
        });
    }


    public void openMasterKeyPopUp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.popup_master_key, null);
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        view.findViewById(R.id.btnSubmit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //sharePost(context);
            }
        });

        dialog.show();
    }
}