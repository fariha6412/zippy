package com.example.zippy.ui.extras;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageButton;

import com.example.zippy.R;

public class AboutActivity extends AppCompatActivity {

    ImageButton backbtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        backbtn = findViewById(R.id.backArrow);
        backbtn.setOnClickListener(v -> {
            finish();
        });
    }
}