package com.example.zippy.ui.setting;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

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

import com.example.zippy.MainActivity;
import com.example.zippy.R;
import com.example.zippy.helper.BottomNavigationHelper;
import com.example.zippy.helper.MenuHelperClass;
import com.example.zippy.ui.profile.InstructorProfileActivity;
import com.example.zippy.ui.profile.StudentProfileActivity;
import com.example.zippy.utility.NetworkChangeListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

public class SettingsActivity extends AppCompatActivity {
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    BottomNavigationView bottomNavigationView;

    Button changeProfilePicBtn, changeUserNameBtn, changeDesignationBtn, changeInstitutionBtn, changeEmployeeIDBtn, changeRegistrationNoBtn;
    Switch darkThemeSwitch, profileLockSwitch;

    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseDatabase rootNode;
    DatabaseReference referenceStudent;

    SharedPreferences mPrefs;
    final String loggedStatus = "loggedProfile";
    final String lockedStatus = "lockedProfile";
    final String darkThemeStatus = "darkTheme";

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar mtoolbar = findViewById(R.id.mtoolbar);
        setSupportActionBar(mtoolbar);
        MenuHelperClass menuHelperClass = new MenuHelperClass(mtoolbar, this);
        menuHelperClass.handle();

        changeProfilePicBtn = findViewById(R.id.changeprofilepicturebtn);
        changeUserNameBtn = findViewById(R.id.changeusernamebtn);
        changeInstitutionBtn = findViewById(R.id.changeinstitutionbtn);
        changeDesignationBtn = findViewById(R.id.changedeisgnationbtn);
        changeRegistrationNoBtn = findViewById(R.id.changeregistrationnobtn);
        changeEmployeeIDBtn = findViewById(R.id.changeemployeeidbtn);
        darkThemeSwitch = findViewById(R.id.darkThemeSwitch);
        profileLockSwitch = findViewById(R.id.profileLockSwitch);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String loggedProfile = mPrefs.getString(loggedStatus, "nouser");
        Boolean lockedProfile = mPrefs.getBoolean(lockedStatus, false);
        Boolean darkTheme = mPrefs.getBoolean(darkThemeStatus, false);

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

        bottomNavigationView = findViewById(R.id.bottom_navigatin_view);
        BottomNavigationHelper bottomNavigationHelper = new BottomNavigationHelper(bottomNavigationView, this);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(3);
        menuItem.setChecked(true);
        bottomNavigationHelper.handle();
        //bottomNavigationView.setSelectedItemId(R.id.navigation_settings);

        changeProfilePicBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, ChangeProfilePictureActivity.class));
        });

        profileLockSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println();
                if(profileLockSwitch.isChecked()){
                    mPrefs.edit().putBoolean(lockedStatus, true).apply();
                    writeLocked(true);
                }
                else {
                    writeLocked(false);
                    mPrefs.edit().putBoolean(lockedStatus, false).apply();
                }
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
            System.out.println("dark: "+darkThemeSwitch.isChecked());
        });

    }
    private void writeLocked(Boolean isLocked){
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        rootNode = FirebaseDatabase.getInstance();
        referenceStudent = rootNode.getReference("students/"+user.getUid());
        referenceStudent.child("isProfileLocked").setValue(isLocked, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable @org.jetbrains.annotations.Nullable DatabaseError error, @NonNull @NotNull DatabaseReference ref) {
                if(error != null){
                    System.out.println(error.toString());
                }
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