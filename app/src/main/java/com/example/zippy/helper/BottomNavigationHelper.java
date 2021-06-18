package com.example.zippy.helper;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;

import com.example.zippy.ui.chat.ChatActivity;
import com.example.zippy.MainActivity;
import com.example.zippy.R;
import com.example.zippy.ui.search.SearchActivity;
import com.example.zippy.ui.setting.SettingsActivity;
import com.example.zippy.ui.profile.InstructorProfileActivity;
import com.example.zippy.ui.profile.StudentProfileActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class BottomNavigationHelper {
    BottomNavigationView bottomNavigationView;
    Activity activity;

    private FirebaseAuth auth;
    private FirebaseUser user;
    private final SharedPreferences mPrefs;
    final String loggedStatus = "loggedProfile";

    public BottomNavigationHelper(BottomNavigationView bottomNavigationView, Activity activity) {
        this.bottomNavigationView = bottomNavigationView;
        this.activity = activity;
        mPrefs = PreferenceManager.getDefaultSharedPreferences(activity);
    }

    public void handle(){
        bottomNavigationView.setOnItemSelectedListener(this::onItemClick);
    }

    public boolean onItemClick(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.navigation_profile:
                String loggedProfile = mPrefs.getString(loggedStatus, "nouser");
                auth = FirebaseAuth.getInstance();
                user = auth.getCurrentUser();
                if(user!=null){
                    if(loggedProfile.equals("instructor")){
                        //bottomNavigationView.setSelectedItemId(R.id.navigation_settings);
                        if(bottomNavigationView.getSelectedItemId()==item.getItemId())return true;

                        activity.startActivity(new Intent(activity.getApplicationContext(), InstructorProfileActivity.class));
                        activity.overridePendingTransition(0,0);
                        return true;
                    }
                    if(loggedProfile.equals("student")){
                        if(bottomNavigationView.getSelectedItemId()==item.getItemId())return true;

                        activity.startActivity(new Intent(activity.getApplicationContext(), StudentProfileActivity.class));
                        activity.overridePendingTransition(0,0);
                        return true;
                    }
                }
            case R.id.navigation_chat:
                if(bottomNavigationView.getSelectedItemId()==item.getItemId())return true;

                activity.startActivity(new Intent(activity.getApplicationContext(), ChatActivity.class));
                activity.overridePendingTransition(0,0);
                return true;
            case R.id.navigation_search:
                if(bottomNavigationView.getSelectedItemId()==item.getItemId())return true;

                activity.startActivity(new Intent(activity.getApplicationContext(), SearchActivity.class));
                activity.overridePendingTransition(0,0);
                return true;
            case R.id.navigation_settings:
                if(bottomNavigationView.getSelectedItemId()==item.getItemId())return true;

                activity.startActivity(new Intent(activity.getApplicationContext(), SettingsActivity.class));
                activity.overridePendingTransition(0,0);
                return true;
            case R.id.navigation_refresh:
                backToProfile(bottomNavigationView, activity);
                return true;
        }
        return false;
    }
    public static void backToProfile(BottomNavigationView bottomNavigationView, Activity activity){
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(0);
        menuItem.setChecked(true);
        activity.startActivity(new Intent(activity.getApplicationContext(), MainActivity.class));
        activity.overridePendingTransition(0,0);
    }
}
