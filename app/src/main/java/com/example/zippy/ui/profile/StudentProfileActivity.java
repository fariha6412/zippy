package com.example.zippy.ui.profile;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.zippy.AboutActivity;
import com.example.zippy.CourseCreationActivity;
import com.example.zippy.CourseEnrollActivity;
import com.example.zippy.MainActivity;
import com.example.zippy.R;
import com.example.zippy.helper.StudentHelperClass;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

public class StudentProfileActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseDatabase rootNode;
    DatabaseReference referenceStudent, referenceCourse;
    FirebaseUser user;

    TextView txtViewFullName, txtViewInstitution, txtViewRegistrationNo;
    ImageView img;
    MaterialButton addbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_profile);
        showProfile();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menuabout:
                startActivity(new Intent(StudentProfileActivity.this, AboutActivity.class));
                return true;
            case R.id.menuexit:
                onBackPressed();
                return true;
            case R.id.menulogout:
                signOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
        txtViewRegistrationNo = findViewById(R.id.txtviewregistraionno);
        img = findViewById(R.id.imgview);
        addbtn = findViewById(R.id.addbtn);

        rootNode = FirebaseDatabase.getInstance();
        referenceStudent = rootNode.getReference("students/"+ user.getUid());
        referenceCourse = rootNode.getReference("courses");

        Toolbar mtoolbar = findViewById(R.id.mtoolbar);

        setSupportActionBar(mtoolbar);

        referenceStudent.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                StudentHelperClass value = dataSnapshot.getValue(StudentHelperClass.class);
                if(value!=null){
                    txtViewFullName.setText(value.getFullName());
                    txtViewRegistrationNo.setText("RegistrationNO: "+value.getRegistrationNo());
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
        addbtn.setOnClickListener(v -> {
            startActivity(new Intent(StudentProfileActivity.this, CourseEnrollActivity.class));
        });
    }
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Message")
                .setMessage("Do you want to exit app?")
                .setNegativeButton("NO", null)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finishAffinity();
                    }
                }).create().show();
    }
    public void signOut(){
        new AlertDialog.Builder(this)
                .setTitle("Message")
                .setMessage("Do you want to log out?")
                .setNegativeButton("NO", null)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(StudentProfileActivity.this, MainActivity.class));
                    }
                }).create().show();
    }
}