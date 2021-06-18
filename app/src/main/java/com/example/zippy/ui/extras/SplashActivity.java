package com.example.zippy.ui.extras;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.example.zippy.R;
import com.example.zippy.ui.register.ChooseAccountTypeActivity;

public class SplashActivity extends AppCompatActivity {

    Button getStarted;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        getStarted = findViewById(R.id.getstartedbtn);
        getStarted.setOnClickListener(v -> {
            Intent intent=new Intent(SplashActivity.this, ChooseAccountTypeActivity.class);
            startActivity(intent);
            finish();
        });
    }
}