package com.example.zippy;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.zippy.ui.register.RegisterInstructorActivity;
import com.example.zippy.ui.register.RegisterStudentActivity;


public class ChooseAccountTypeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);

        Toolbar mtoolbar = findViewById(R.id.mtoolbar);
        setSupportActionBar(mtoolbar);
        TextView txtviewlogin = findViewById(R.id.txtviewlogin);
        Button instructorbtn = findViewById(R.id.instructorbtn);
        Button studentbtn = findViewById(R.id.studentbtn);

        txtviewlogin.setOnClickListener(v -> startActivity(new Intent(ChooseAccountTypeActivity.this, MainActivity.class)));
        instructorbtn.setOnClickListener(v -> startActivity(new Intent(ChooseAccountTypeActivity.this, RegisterInstructorActivity.class)));
        studentbtn.setOnClickListener(v -> startActivity(new Intent(ChooseAccountTypeActivity.this, RegisterStudentActivity.class)));
    }

    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
}