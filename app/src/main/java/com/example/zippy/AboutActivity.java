package com.example.zippy;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageButton;

public class AboutActivity extends AppCompatActivity {

    ImageButton backbtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        backbtn = findViewById(R.id.backarrow);
        backbtn.setOnClickListener(v -> {
            finish();
        });
    }
}