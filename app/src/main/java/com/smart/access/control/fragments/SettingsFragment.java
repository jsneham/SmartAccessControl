package com.smart.access.control.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.smart.access.control.R;
import com.smart.access.control.adapters.CustomAdapter;
import com.smart.access.control.modals.SettingsMenu;

import java.util.ArrayList;


public class SettingsFragment extends Fragment {


    private View view;
    private String tag;
    private RecyclerView rvMenu;
    private CustomAdapter customAdapter;
    private ArrayList<SettingsMenu> settingsMenus;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_settings, container, false);

        getData();

        setRecylerView();

//        recyclerItemClick();
        return view;
    }

    private void setRecylerView() {
        rvMenu = (RecyclerView) view.findViewById(R.id.rvMenu);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        rvMenu.setLayoutManager(mLayoutManager);
        customAdapter = new CustomAdapter(settingsMenus);
        rvMenu.setAdapter(customAdapter);
    }

    private void getData() {
        settingsMenus = new ArrayList<>();
        settingsMenus.add(new SettingsMenu(R.drawable.ic_baseline_question_mark_24, "FAQs", "Frequently asked questions can be found here"));
        settingsMenus.add(new SettingsMenu(R.drawable.ic_baseline_info_24, "About Us", "Know more about us"));
        settingsMenus.add(new SettingsMenu(R.drawable.ic_baseline_note_24, "Terms & Conditions", "Terms of Service, Usage & other conditions"));
        settingsMenus.add(new SettingsMenu(R.drawable.ic_baseline_remove_red_eye_24, "Privacy Policy", "Data policy, consents & more"));
        settingsMenus.add(new SettingsMenu(R.drawable.ic_baseline_logout_24, "LOGOUT", "Logout from this device"));
    }

}