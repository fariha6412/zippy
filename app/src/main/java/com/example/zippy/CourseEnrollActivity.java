package com.example.zippy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.zippy.helper.CourseHelperClass;
import com.example.zippy.helper.InstructorHelperClass;
import com.example.zippy.helper.StudentHelperClass;
import com.example.zippy.helper.ValidationChecker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

public class CourseEnrollActivity extends AppCompatActivity {
    FirebaseAuth auth;
    FirebaseDatabase rootNode;
    DatabaseReference referenceStudent, referenceStudentNoOfCourses, referenceStudentCourses, referenceCourse, referenceCourseNoOfStudents;
    FirebaseUser user;

    EditText editTXTCoursePassCode;
    Button enrollbtn, cancelbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_enroll);
        Toolbar mtoolbar = findViewById(R.id.mtoolbar);
        setSupportActionBar(mtoolbar);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        rootNode = FirebaseDatabase.getInstance();
        referenceStudent = rootNode.getReference("students/"+ user.getUid());
        referenceStudentNoOfCourses = rootNode.getReference("students/"+ user.getUid()+"/noOfCourses");
        referenceStudentCourses = rootNode.getReference("students/"+ user.getUid()+"/courses");
        referenceCourse = rootNode.getReference("courses");

        editTXTCoursePassCode = findViewById(R.id.edittxtcoursepasscode);
        enrollbtn = findViewById(R.id.enrollbtn);
        cancelbtn = findViewById(R.id.cancelbtn);

        cancelbtn.setOnClickListener(v -> {
            finish();
        });
        enrollbtn.setOnClickListener(v -> {
            final boolean[] canEnroll = {false};
            final Long[] noOfCourses = new Long[1];
            final Long[] noOfStudents = new Long[1];
            String coursePassCode = editTXTCoursePassCode.getText().toString().trim();
            if(ValidationChecker.isFieldEmpty(coursePassCode, editTXTCoursePassCode))return;
            /*referenceCourse.child(coursePassCode).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NotNull DataSnapshot snapshot) {
                    if(!snapshot.exists()){
                        editTXTCoursePassCode.setError("No course found");
                        editTXTCoursePassCode.requestFocus();
                    }
                    else {
                        canEnroll[0] = true;
                        System.out.println("sdfdsfdsf");
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                    Log.w("Error","somethings wrong");
                }
            });*/

            System.out.println("baire");
            if(isCourseExists(coursePassCode)){
                System.out.println("hidsfdsafdsfdsfdsfdsfdsfdssdfffffff");

                referenceCourseNoOfStudents = rootNode.getReference("courses"+coursePassCode+"/noOfStudents");
                referenceCourse.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                        // This method is called once with the initial value and again
                        // whenever data at this location is updated.
                        CourseHelperClass value = dataSnapshot.getValue(CourseHelperClass.class);
                        assert value != null;
                        noOfStudents[0] = (value.getNoOfStudents()) + 1L;
                    }

                    @Override
                    public void onCancelled(@NotNull DatabaseError error) {
                        // Failed to read value
                        Log.w("Error", "Failed to read value.", error.toException());
                    }
                });
                referenceStudent.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                        // This method is called once with the initial value and again
                        // whenever data at this location is updated.
                        StudentHelperClass value = dataSnapshot.getValue(StudentHelperClass.class);
                        assert value != null;
                        noOfCourses[0] = (value.getNoOfCourses()) + 1L;
                    }

                    @Override
                    public void onCancelled(@NotNull DatabaseError error) {
                        // Failed to read value
                        Log.w("Error", "Failed to read value.", error.toException());
                    }
                });
                referenceCourseNoOfStudents.setValue((noOfStudents[0]),new DatabaseReference.CompletionListener(){

                    @Override
                    public void onComplete(@Nullable @org.jetbrains.annotations.Nullable DatabaseError error, @NonNull @NotNull DatabaseReference ref) {
                        System.err.println("Value was set. Error = "+error);
                    }
                });
                referenceStudentNoOfCourses.setValue((noOfCourses[0]),new DatabaseReference.CompletionListener(){

                    @Override
                    public void onComplete(@Nullable @org.jetbrains.annotations.Nullable DatabaseError error, @NonNull @NotNull DatabaseReference ref) {
                        System.err.println("Value was set. Error = "+error);
                    }
                });
                String strNoOfCourses = String.valueOf(noOfCourses[0]);
                referenceStudentCourses.child(strNoOfCourses).setValue(coursePassCode, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable @org.jetbrains.annotations.Nullable DatabaseError error, @NonNull @NotNull DatabaseReference ref) {
                        System.err.println("Value was set. Error = "+error);
                    }
                });
                Toast.makeText(getApplicationContext(), "Successfully Enrolled", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    public boolean isCourseExists(String coursePassCode){
        final boolean[] result = {false};
        referenceCourse.child(coursePassCode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    editTXTCoursePassCode.setError("No course found");
                    editTXTCoursePassCode.requestFocus();
                    result[0] = false;
                }
                else {
                    System.out.println("sdfdsfdsf");
                    result[0] = true;
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Log.w("Error","somethings wrong");
            }
        });
        return result[0];
    }
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }
}