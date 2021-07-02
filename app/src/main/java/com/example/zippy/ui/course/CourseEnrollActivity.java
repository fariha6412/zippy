package com.example.zippy.ui.course;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.zippy.R;
import com.example.zippy.helper.CourseHelperClass;
import com.example.zippy.helper.MenuHelperClass;
import com.example.zippy.helper.StudentHelperClass;
import com.example.zippy.helper.ValidationChecker;
import com.example.zippy.ui.profile.UserProfileActivity;
import com.example.zippy.utility.NetworkChangeListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

public class CourseEnrollActivity extends AppCompatActivity {
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    private DatabaseReference referenceStudentCourses;
    private DatabaseReference referenceCourse, referenceStudent;
    private FirebaseUser user;

    private EditText editTXTCoursePassCode;
    private static Integer flag = 1;
    private Boolean canEnroll = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_enroll);
        Toolbar toolbar = findViewById(R.id.mToolbar);
        setSupportActionBar(toolbar);
        MenuHelperClass menuHelperClass = new MenuHelperClass(toolbar, this);
        menuHelperClass.handle();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        FirebaseDatabase rootNode = FirebaseDatabase.getInstance();
        referenceStudent = rootNode.getReference("students/" + user.getUid());
        referenceStudentCourses = referenceStudent.child("courses");
        referenceCourse = rootNode.getReference("courses");
        editTXTCoursePassCode = findViewById(R.id.edittxtcoursepasscode);
        Button enrollBtn = findViewById(R.id.enrollbtn);
        Button cancelBtn = findViewById(R.id.cancelbtn);

        final Long[] noOfCourses = new Long[1];
        referenceStudent.child("noOfCourses").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) noOfCourses[0] = (Long) dataSnapshot.getValue() + 1L;
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Log.w("Error", "somethings wrong");
            }
        });

        cancelBtn.setOnClickListener(v -> {
            finish();
        });
        enrollBtn.setOnClickListener(v -> {
            canEnroll = false;
            final Long[] noOfStudents = new Long[1];
            String coursePassCode = editTXTCoursePassCode.getText().toString().trim();
            if(ValidationChecker.isFieldEmpty(coursePassCode, editTXTCoursePassCode))return;
            referenceCourse.child(coursePassCode).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NotNull DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        editTXTCoursePassCode.setError("No course found");
                        editTXTCoursePassCode.requestFocus();
                    } else {
                        try{
                            if((boolean) snapshot.child("isCompleted").getValue()){
                                Toast.makeText(CourseEnrollActivity.this, "Course is completed", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }
                        catch (NullPointerException e){
                            //pass
                        }
                        noOfStudents[0] = (Long) snapshot.child("noOfStudents").getValue() + 1L;
                        final boolean[] isAlreadyEnrolled = {false};
                        referenceStudentCourses.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot dsnap:dataSnapshot.getChildren()) {
                                    String dsnapPassCode = (String) dsnap.getValue();
                                    assert dsnapPassCode != null;
                                    if (dsnapPassCode.equals(coursePassCode)) {
                                        isAlreadyEnrolled[0] = true;
                                    }
                                }
                                if(!isAlreadyEnrolled[0]){

                                    referenceCourse.child(coursePassCode).child("blockedStudents").child(user.getUid()).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull @NotNull DataSnapshot snapt) {
                                            if(snapt.exists()){
                                                if((boolean)snapt.getValue()){
                                                    Toast.makeText(CourseEnrollActivity.this, "You have been blocked from this course", Toast.LENGTH_SHORT).show();
                                                    finish();
                                                }
                                                else enroll(coursePassCode, noOfStudents, noOfCourses);
                                            }
                                            else enroll(coursePassCode, noOfStudents, noOfCourses);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                        }
                                    });
                                }
                                else{
                                    Toast.makeText(getApplicationContext(), "Enrolled", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                                Log.w("Error","somethings wrong");
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                    Log.w("Error","somethings wrong");
                }
            });
        });
    }
    private void enroll(String coursePassCode, Long[] noOfStudents, Long[] noOfCourses){

        referenceCourse.child(coursePassCode).child("noOfStudents").setValue((noOfStudents[0]), new DatabaseReference.CompletionListener() {

            @Override
            public void onComplete(@Nullable @org.jetbrains.annotations.Nullable DatabaseError error, @NonNull @NotNull DatabaseReference ref) {
                System.err.println("Value was set. Error = " + error);
            }
        });
        referenceStudent.child("noOfCourses").setValue((noOfCourses[0]), new DatabaseReference.CompletionListener() {

            @Override
            public void onComplete(@Nullable @org.jetbrains.annotations.Nullable DatabaseError error, @NonNull @NotNull DatabaseReference ref) {
                System.err.println("Value was set. Error = " + error);
            }
        });
        referenceStudentCourses.push().setValue(coursePassCode, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable @org.jetbrains.annotations.Nullable DatabaseError error, @NonNull @NotNull DatabaseReference ref) {
                System.err.println("Value was set. Error = " + error);
            }
        });

        referenceCourse.child(coursePassCode).child("students").push().setValue(user.getUid(), new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable @org.jetbrains.annotations.Nullable DatabaseError error, @NonNull @NotNull DatabaseReference ref) {
                System.err.println("Value was set. Error = " + error);
            }
        });
        //Toast.makeText(getApplicationContext(), "Successfully Enrolled", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(CourseEnrollActivity.this, UserProfileActivity.class));
    }
    //internet related stuff
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
    //end stuff
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }
}