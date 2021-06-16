package com.example.zippy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.zippy.helper.CourseHelperClass;
import com.example.zippy.helper.InstructorHelperClass;
import com.example.zippy.helper.MenuHelperClass;
import com.example.zippy.helper.PassCodeGenerator;
import com.example.zippy.helper.ValidationChecker;
import com.example.zippy.utility.NetworkChangeListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

public class CourseCreationActivity extends AppCompatActivity {
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    FirebaseAuth auth;
    FirebaseDatabase rootNode;
    DatabaseReference referenceInstructor, referenceInstructorNoOfCourses, referenceInstructorCourses, referenceCourse;
    FirebaseUser user;

    EditText edtTXTCourseCode, edtTXTCourseTitle, editTXTCourseYear, editTXTCoursePassCode, editTXTCourseCredit;
    Button createbtn, cancelbtn;
    MaterialButton regeneratePassCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_creation);
        Toolbar mtoolbar = findViewById(R.id.mtoolbar);
        setSupportActionBar(mtoolbar);

        edtTXTCourseCode = findViewById(R.id.edittxtcoursecode);
        edtTXTCourseTitle = findViewById(R.id.edittxtcoursetitle);
        editTXTCoursePassCode = findViewById(R.id.edittxtcoursepasscode);
        editTXTCourseYear = findViewById(R.id.edittxtcourseyear);
        editTXTCourseCredit = findViewById(R.id.edittxtcoursecredit);
        createbtn = findViewById(R.id.createbtn);
        cancelbtn = findViewById(R.id.cancelbtn);
        regeneratePassCode = findViewById(R.id.regeneratepasscode);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        rootNode = FirebaseDatabase.getInstance();
        referenceInstructor = rootNode.getReference("instructors/"+ user.getUid());
        referenceInstructorNoOfCourses = rootNode.getReference("instructors/"+ user.getUid()+"/noOfCourses");
        referenceInstructorCourses = rootNode.getReference("instructors/"+ user.getUid()+"/courses");
        referenceCourse = rootNode.getReference("courses");
        final Long[] noOfCourses = new Long[1];

        editTXTCoursePassCode.setText(createNewPassCode());


        referenceInstructor.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                InstructorHelperClass value = dataSnapshot.getValue(InstructorHelperClass.class);
                assert value != null;
                noOfCourses[0] = (value.getNoOfCourses()) + 1L;
            }

            @Override
            public void onCancelled(@NotNull DatabaseError error) {
                // Failed to read value
                Log.w("Error", "Failed to read value.", error.toException());
            }
        });

        regeneratePassCode.setOnClickListener(v -> {
            editTXTCoursePassCode.setText(createNewPassCode());
        });

        createbtn.setOnClickListener(v -> {

            String courseCode = edtTXTCourseCode.getText().toString().trim();
            String courseTitle = edtTXTCourseTitle.getText().toString().trim();
            String courseYear = editTXTCourseYear.getText().toString().trim();
            String courseCredit = editTXTCourseCredit.getText().toString().trim();
            String coursePassCode = editTXTCoursePassCode.getText().toString().trim();
            String instructoruid = user.getUid();

            if(ValidationChecker.isFieldEmpty(courseCode, edtTXTCourseCode))return;
            if(ValidationChecker.isFieldEmpty(courseTitle, edtTXTCourseTitle))return;
            if(ValidationChecker.isFieldEmpty(courseYear, editTXTCourseYear))return;
            if(ValidationChecker.isFieldEmpty(courseCredit, editTXTCourseCredit))return;
            if(coursePassCode.isEmpty()){
                coursePassCode = createNewPassCode();
                editTXTCoursePassCode.setText(coursePassCode);
            }
            String finalCoursePassCode = coursePassCode;
            referenceCourse.child(coursePassCode).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        editTXTCoursePassCode.setError("Pass code already exists. Please regenerate");
                        editTXTCoursePassCode.requestFocus();
                    }
                    else{
                        CourseHelperClass courseHelper = new CourseHelperClass(courseCode, courseTitle, courseYear, courseCredit, finalCoursePassCode, instructoruid);
                        referenceCourse.child(finalCoursePassCode).setValue(courseHelper, new DatabaseReference.CompletionListener(){

                            @Override
                            public void onComplete(@Nullable @org.jetbrains.annotations.Nullable DatabaseError error, @NonNull @NotNull DatabaseReference ref) {
                                System.err.println("Value was set. Error = "+error);
                            }
                        });

                        referenceInstructorNoOfCourses.setValue((noOfCourses[0]),new DatabaseReference.CompletionListener(){

                            @Override
                            public void onComplete(@Nullable @org.jetbrains.annotations.Nullable DatabaseError error, @NonNull @NotNull DatabaseReference ref) {
                                System.err.println("Value was set. Error = "+error);
                            }
                        });
                        String strNoOfCourses = String.valueOf(noOfCourses[0]);
                        referenceInstructorCourses.child(strNoOfCourses).setValue(finalCoursePassCode, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable @org.jetbrains.annotations.Nullable DatabaseError error, @NonNull @NotNull DatabaseReference ref) {
                                System.err.println("Value was set. Error = "+error);
                            }
                        });
                        Toast.makeText(getApplicationContext(), "Course Created", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                    Log.w("Error","somethings wrong");
                }
            });
        });

        cancelbtn.setOnClickListener(v -> {
            finish();
        });

    }
    public String createNewPassCode(){
        PassCodeGenerator passwordGenerator = new PassCodeGenerator.PassCodeGeneratorBuilder()
                .useDigits(true)
                .useLower(true)
                .useUpper(true)
                .usePunctuation(true)
                .build();
        return passwordGenerator.generate(8);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menuabout:
                MenuHelperClass.showAbout(this);
                return true;
            case R.id.menuexit:
                MenuHelperClass.exit(this);
                return true;
            case R.id.menulogout:
                MenuHelperClass.signOut(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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