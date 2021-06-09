package com.example.zippy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.zippy.helper.CourseHelperClass;
import com.example.zippy.helper.InstructorHelperClass;
import com.example.zippy.helper.StudentHelperClass;
import com.example.zippy.helper.ValidationChecker;
import com.example.zippy.ui.change.ChangeProfilePictureActivity;
import com.example.zippy.ui.profile.StudentProfileActivity;
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
    DatabaseReference referenceStudent, referenceStudentNoOfCourses, referenceStudentCourses;
    DatabaseReference referenceCourse, referenceCourseNoOfStudents, referenceCourseStudents;
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
        referenceStudentCourses = rootNode.getReference("students/"+ user.getUid()+"/courses");
        referenceStudentNoOfCourses = rootNode.getReference("students/"+user.getUid()+"/noOfCourses");
        referenceCourse = rootNode.getReference("courses");
        editTXTCoursePassCode = findViewById(R.id.edittxtcoursepasscode);
        enrollbtn = findViewById(R.id.enrollbtn);
        cancelbtn = findViewById(R.id.cancelbtn);

        final Long[] noOfCourses = new Long[1];
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
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Log.w("Error", "somethings wrong");
            }
        });

        cancelbtn.setOnClickListener(v -> {
            finish();
        });
        enrollbtn.setOnClickListener(v -> {
            final Long[] noOfStudents = new Long[1];
            String coursePassCode = editTXTCoursePassCode.getText().toString().trim();
            if(ValidationChecker.isFieldEmpty(coursePassCode, editTXTCoursePassCode))return;
            referenceCourse.child(coursePassCode).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NotNull DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        editTXTCoursePassCode.setError("No course found");
                        editTXTCoursePassCode.requestFocus();
                    } else {

                        CourseHelperClass value = snapshot.getValue(CourseHelperClass.class);
                        assert value != null;
                        System.out.println(value.toString());
                        System.out.println(value.getNoOfStudents());
                        noOfStudents[0] = (value.getNoOfStudents()) + 1L;
                        referenceCourseNoOfStudents = rootNode.getReference("courses/"+coursePassCode+"/noOfStudents");
                        referenceCourseNoOfStudents.setValue((noOfStudents[0]), new DatabaseReference.CompletionListener() {

                            @Override
                            public void onComplete(@Nullable @org.jetbrains.annotations.Nullable DatabaseError error, @NonNull @NotNull DatabaseReference ref) {
                                System.err.println("Value was set. Error = " + error);
                            }
                        });
                        referenceStudentNoOfCourses.setValue((noOfCourses[0]), new DatabaseReference.CompletionListener() {

                            @Override
                            public void onComplete(@Nullable @org.jetbrains.annotations.Nullable DatabaseError error, @NonNull @NotNull DatabaseReference ref) {
                                System.err.println("Value was set. Error = " + error);
                            }
                        });
                        referenceStudentCourses.child(String.valueOf(noOfCourses[0])).setValue(coursePassCode, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable @org.jetbrains.annotations.Nullable DatabaseError error, @NonNull @NotNull DatabaseReference ref) {
                                System.err.println("Value was set. Error = " + error);
                            }
                        });
                        referenceCourseStudents = rootNode.getReference("courses/"+coursePassCode+"/students");
                        referenceCourseStudents.child(String.valueOf(noOfStudents[0])).setValue(user.getUid(), new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable @org.jetbrains.annotations.Nullable DatabaseError error, @NonNull @NotNull DatabaseReference ref) {
                                System.err.println("Value was set. Error = " + error);
                            }
                        });
                        Toast.makeText(getApplicationContext(), "Successfully Enrolled", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                    Log.w("Error","somethings wrong");
                }
            });
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menuabout:
                startActivity(new Intent(CourseEnrollActivity.this, AboutActivity.class));
                return true;
            case R.id.menuexit:
                exit();
                return true;
            case R.id.menulogout:
                signOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
                        startActivity(new Intent(CourseEnrollActivity.this, MainActivity.class));
                    }
                }).create().show();
    }
    public void exit(){
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
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }
}