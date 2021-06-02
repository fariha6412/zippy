package com.example.zippy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ChooseAccountType extends AppCompatActivity {

    private Toolbar mtoolbar;
    private TextView txtviewlogin;
    private Button instructorbtn, studentbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);

        mtoolbar = findViewById(R.id.mtoolbar);
        setSupportActionBar(mtoolbar);
        txtviewlogin = findViewById(R.id.txtviewlogin);
        instructorbtn = findViewById(R.id.instructorbtn);
        studentbtn = findViewById(R.id.studentbtn);

        txtviewlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ChooseAccountType.this, MainActivity.class));
            }
        });

        instructorbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ChooseAccountType.this, RegisterInstructor.class));
            }
        });
        studentbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ChooseAccountType.this, RegisterStudent.class));
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
}