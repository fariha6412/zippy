package com.example.zippy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;

import kotlinx.coroutines.Delay;

public class SplashActivity extends AppCompatActivity {

    Button getStarted;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        getStarted = findViewById(R.id.getstartedbtn);
        getStarted.setOnClickListener(v -> {
            Intent intent=new Intent(SplashActivity.this,ChooseAccountTypeActivity.class);
            startActivity(intent);
            finish();
        });
    }
}