package com.example.zippy.ui.setting;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.example.zippy.R;
import com.example.zippy.helper.BottomNavigationHelper;
import com.example.zippy.helper.MenuHelperClass;
import com.example.zippy.utility.NetworkChangeListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SettingsActivity extends AppCompatActivity {
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    private BottomNavigationView bottomNavigationView;

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch darkThemeSwitch, profileLockSwitch;

    private SharedPreferences mPrefs;
    final String loggedStatus = "loggedProfile";
    final String lockedStatus = "lockedProfile";
    final String darkThemeStatus = "darkTheme";

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.mToolbar);
        setSupportActionBar(toolbar);
        MenuHelperClass menuHelperClass = new MenuHelperClass(toolbar, this);
        menuHelperClass.handle();

        Button changeProfilePicBtn = findViewById(R.id.changeprofilepicturebtn);
        Button changeUserNameBtn = findViewById(R.id.changeusernamebtn);
        Button changeInstitutionBtn = findViewById(R.id.changeinstitutionbtn);
        Button changeDesignationBtn = findViewById(R.id.changedeisgnationbtn);
        Button changeRegistrationNoBtn = findViewById(R.id.changeregistrationnobtn);
        Button changeEmployeeIDBtn = findViewById(R.id.changeemployeeidbtn);
        darkThemeSwitch = findViewById(R.id.darkThemeSwitch);
        profileLockSwitch = findViewById(R.id.profileLockSwitch);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String loggedProfile = mPrefs.getString(loggedStatus, "nouser");
        boolean lockedProfile = mPrefs.getBoolean(lockedStatus, false);
        boolean darkTheme = mPrefs.getBoolean(darkThemeStatus, false);

        if(loggedProfile.equals("instructor")){
            changeDesignationBtn.setVisibility(View.VISIBLE);
            changeEmployeeIDBtn.setVisibility(View.VISIBLE);
        }
        if(loggedProfile.equals("student")){
            changeRegistrationNoBtn.setVisibility(View.VISIBLE);
            profileLockSwitch.setVisibility(View.VISIBLE);
        }
        if(lockedProfile){
            profileLockSwitch.setChecked(true);
        }
        if(darkTheme){
            darkThemeSwitch.setChecked(true);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        BottomNavigationHelper bottomNavigationHelper = new BottomNavigationHelper(bottomNavigationView, this);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(3);
        menuItem.setChecked(true);
        bottomNavigationHelper.handle();

        changeProfilePicBtn.setOnClickListener(v -> startActivity(new Intent(this, ChangeProfilePictureActivity.class)));

        profileLockSwitch.setOnClickListener(v -> {
            System.out.println();
            if(profileLockSwitch.isChecked()){
                mPrefs.edit().putBoolean(lockedStatus, true).apply();
                writeLocked(true);
            }
            else {
                writeLocked(false);
                mPrefs.edit().putBoolean(lockedStatus, false).apply();
            }
        });

        darkThemeSwitch.setOnClickListener(v -> {
            if(darkThemeSwitch.isChecked()){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                mPrefs.edit().putBoolean(darkThemeStatus, true).apply();
            }
            else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                mPrefs.edit().putBoolean(darkThemeStatus, false).apply();
            }
        });

    }
    private void writeLocked(Boolean isLocked){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        FirebaseDatabase rootNode = FirebaseDatabase.getInstance();
        assert user != null;
        DatabaseReference referenceStudent = rootNode.getReference("students/" + user.getUid());
        referenceStudent.child("isProfileLocked").setValue(isLocked, (error, ref) -> {
            if(error != null){
                System.out.println(error.toString());
            }
        });
    }
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }
    @Override
    protected void onStart() {
        IntentFilter filter=new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener,filter);
        super.onStart();
    }
    @Override
    protected void onStop(){
        unregisterReceiver(networkChangeListener);
        super.onStop();
    }
    @Override
    public void onBackPressed() {
        BottomNavigationHelper.backToProfile(bottomNavigationView, this);
    }
}