package com.example.zippy.ui.profile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.zippy.CourseCreationActivity;
import com.example.zippy.MainActivity;
import com.example.zippy.R;
import com.example.zippy.helper.InstructorHelperClass;
import com.example.zippy.helper.StudentHelperClass;
import com.example.zippy.ui.register.RegisterStudentActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class InstructorProfileActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseDatabase rootNode;
    DatabaseReference reference;
    FirebaseUser user;

    TextView txtViewFullName, txtViewInstitution, txtViewDesignation, txtViewEmplyeeID;
    ImageView img;
    MaterialButton createbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructor_profile);
        showProfile();
    }
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }
    public void showProfile(){
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        txtViewFullName = findViewById(R.id.txtviewfullname);
        txtViewInstitution = findViewById(R.id.txtviewinstitution);
        txtViewDesignation = findViewById(R.id.txtviewdesignation);
        txtViewEmplyeeID = findViewById(R.id.txtviewemployeeid);
        img = findViewById(R.id.imgview);
        createbtn = findViewById(R.id.createbtn);

        rootNode = FirebaseDatabase.getInstance();
        reference = rootNode.getReference("instructors/"+ user.getUid());

        Toolbar mtoolbar = findViewById(R.id.mtoolbar);

        setSupportActionBar(mtoolbar);

        createbtn.setOnClickListener(v -> {
            startActivity(new Intent(InstructorProfileActivity.this, CourseCreationActivity.class));
        });

        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                InstructorHelperClass value = dataSnapshot.getValue(InstructorHelperClass.class);
                if(value!=null){
                    txtViewFullName.setText(value.getFullName());
                    txtViewDesignation.setText(value.getDesignation());
                    txtViewEmplyeeID.setText("EmployeeID: "+value.getEmployeeID());
                    txtViewInstitution.setText(value.getInstitution());

                    Glide.with(getBaseContext()).load(value.getImage()).into(img);
                    Log.d("Response", "Value is: " + value.toString());
                }
            }

            @Override
            public void onCancelled(@NotNull DatabaseError error) {
                // Failed to read value
                Log.w("Error", "Failed to read value.", error.toException());
            }
        });

    }
}