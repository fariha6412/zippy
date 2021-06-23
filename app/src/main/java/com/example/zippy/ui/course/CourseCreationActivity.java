package com.example.zippy.ui.course;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.zippy.R;
import com.example.zippy.helper.CourseHelperClass;
import com.example.zippy.helper.InstructorHelperClass;
import com.example.zippy.helper.MenuHelperClass;
import com.example.zippy.helper.PassCodeGenerator;
import com.example.zippy.helper.ValidationChecker;
import com.example.zippy.utility.NetworkChangeListener;
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

    private DatabaseReference referenceInstructorNoOfCourses;
    private DatabaseReference referenceInstructorCourses;
    private DatabaseReference referenceCourse;
    private FirebaseUser user;

    private EditText edtTXTCourseCode, edtTXTCourseTitle, editTXTCourseYear;
    protected EditText editTXTCoursePassCode, editTXTCourseCredit;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_creation);
        Toolbar toolbar = findViewById(R.id.mtoolbar);
        setSupportActionBar(toolbar);
        MenuHelperClass menuHelperClass = new MenuHelperClass(toolbar, this);
        menuHelperClass.handle();

        edtTXTCourseCode = findViewById(R.id.edittxtcoursecode);
        edtTXTCourseTitle = findViewById(R.id.edittxtcoursetitle);
        editTXTCoursePassCode = findViewById(R.id.edittxtcoursepasscode);
        editTXTCourseYear = findViewById(R.id.edittxtcourseyear);
        editTXTCourseCredit = findViewById(R.id.edittxtcoursecredit);
        Button createBtn = findViewById(R.id.createbtn);
        Button cancelBtn = findViewById(R.id.cancelbtn);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        FirebaseDatabase rootNode = FirebaseDatabase.getInstance();
        DatabaseReference referenceInstructor = rootNode.getReference("instructors/" + user.getUid());
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

        editTXTCoursePassCode.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_RIGHT = 2;

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() >= (editTXTCoursePassCode.getRight() - editTXTCoursePassCode.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        editTXTCoursePassCode.setText(createNewPassCode());
                        return true;
                    }
                }
                return false;
            }
        });

        createBtn.setOnClickListener(v -> {

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
            try {
                double courseCreditDbl = Double.parseDouble(courseCredit);
                if(courseCreditDbl>4 || courseCreditDbl<0.5){
                    editTXTCourseCredit.setError("Enter a valid courseCredit");
                    editTXTCourseCredit.requestFocus();
                    return;
                }
            }
            catch (Exception e){
                editTXTCourseCredit.setError("Enter a Floating point number");
                editTXTCourseCredit.requestFocus();
                return;
            }
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

        cancelBtn.setOnClickListener(v -> {
            finish();
        });

    }
    public String createNewPassCode(){
        PassCodeGenerator passCodeGenerator = new PassCodeGenerator.PassCodeGeneratorBuilder()
                .useDigits(true)
                .useLower(true)
                .useUpper(true)
                .usePunctuation(true)
                .build();
        return passCodeGenerator.generate(8);
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