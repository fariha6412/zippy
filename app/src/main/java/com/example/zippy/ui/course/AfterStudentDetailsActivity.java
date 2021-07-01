package com.example.zippy.ui.course;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.zippy.R;
import com.example.zippy.helper.EmailSender;
import com.example.zippy.helper.MenuHelperClass;
import com.example.zippy.helper.StudentHelperClass;
import com.example.zippy.helper.TestHelperClass;
import com.example.zippy.ui.attendance.AttendanceDetailsActivity;
import com.example.zippy.ui.test.TestDetailsActivity;
import com.example.zippy.utility.NetworkChangeListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

public class AfterStudentDetailsActivity extends AppCompatActivity {
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    SharedPreferences mPrefs;
    final String strClickedUid = "clickedUid";
    private String clickedUid;
    final String strClickedCoursePassCode = "clickedCoursePassCode";
    String clickedCoursePassCode;

    private ImageView imgView;
    private TextView txtViewFullName, txtViewEmail, txtViewInstitution, txtViewRegistrationNo;
    private TextView txtViewAttendancePercentage, txtViewFinalGrade;
    private TextView txtViewYourMarkOnAttendance, txtViewYourTotalMark;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_student_details);
        Toolbar toolbar = findViewById(R.id.mToolbar);
        setSupportActionBar(toolbar);
        MenuHelperClass menuHelperClass = new MenuHelperClass(toolbar, this);
        menuHelperClass.handle();

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        clickedUid = mPrefs.getString(strClickedUid, "");
        clickedCoursePassCode = mPrefs.getString(strClickedCoursePassCode, "");
        imgView = findViewById(R.id.imgView);
        FloatingActionButton floatingActionButton = findViewById(R.id.chatbtn);

        txtViewFullName = findViewById(R.id.txtViewFullName);
        txtViewEmail = findViewById(R.id.txtviewemail);
        txtViewRegistrationNo = findViewById(R.id.txtViewRegistrationNo);
        txtViewInstitution = findViewById(R.id.txtViewInstitution);

        txtViewAttendancePercentage = findViewById(R.id.txtviewattendancepercentage);
        txtViewFinalGrade = findViewById(R.id.txtViewFinalGrade);
        txtViewYourMarkOnAttendance = findViewById(R.id.txtViewResultedMarkOnAttendance);
        txtViewYourTotalMark = findViewById(R.id.txtViewTotalMark);
        txtViewFinalGrade = findViewById(R.id.txtViewFinalGrade);
        LinearLayout markOnAttendanceLinearLayout = findViewById(R.id.resultedMarkOnAttendanceLinearLayout);
        LinearLayout totalMarkLinearLayout = findViewById(R.id.totalMarkLinearLayout);
        LinearLayout finalGradeLinearLayout = findViewById(R.id.finalGradeLinearLayout);

        String strIsCompleted = "isCompleted";
        boolean isCompleted = mPrefs.getBoolean(strIsCompleted, false);

        if(isCompleted){
            totalMarkLinearLayout.setVisibility(View.VISIBLE);
            finalGradeLinearLayout.setVisibility(View.VISIBLE);
            markOnAttendanceLinearLayout.setVisibility(View.VISIBLE);
            getResultData();
        }

        showStudentDetails();

        floatingActionButton.setOnClickListener(v -> {
            EmailSender.sendEmailTo(AfterStudentDetailsActivity.this, new String[] {txtViewEmail.getText().toString().trim()});
        });
    }
    private void showStudentDetails(){
        DatabaseReference referenceStudent = FirebaseDatabase.getInstance().getReference("students/"+clickedUid);

        referenceStudent.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    StudentHelperClass studentHelper = snapshot.getValue(StudentHelperClass.class);
                    assert studentHelper != null;
                    txtViewFullName.setText(studentHelper.getFullName());
                    txtViewInstitution.setText(studentHelper.getInstitution());
                    txtViewEmail.setText(studentHelper.getEmail());

                    txtViewRegistrationNo.setText(studentHelper.getRegistrationNo());
                    Glide.with(getBaseContext()).load(studentHelper.getImage()).into(imgView);
                    extractData();
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
    private void extractData(){
        DatabaseReference referenceAttendance = FirebaseDatabase.getInstance().getReference("attendanceRecord/total/" + clickedCoursePassCode + "/" + clickedUid);
        referenceAttendance.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    txtViewAttendancePercentage.setText(String.valueOf ((Long.parseLong(String.valueOf(snapshot.child("totalPresent").getValue())) *100.0 )/ (Long.parseLong(String.valueOf(snapshot.child("totalPresent").getValue())) + Long.parseLong(String.valueOf(snapshot.child("totalAbsent").getValue())))));
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
    private void getResultData(){
        DatabaseReference referenceResultAttendance = FirebaseDatabase.getInstance().getReference("result/"+clickedUid+"/"+clickedCoursePassCode+"/"+"attendance/got");
        DatabaseReference referenceResultFinalEvaluation = FirebaseDatabase.getInstance().getReference("result/"+clickedUid+"/"+clickedCoursePassCode+"/"+"finalEvaluation");
        referenceResultAttendance.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snpt) {
                if(snpt.exists()){
                    txtViewYourMarkOnAttendance.setText((String.valueOf(snpt.getValue())));
                }
                else txtViewYourMarkOnAttendance.setText("0");
                referenceResultFinalEvaluation.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot dst) {
                        txtViewYourTotalMark.setText(((String) dst.child("got").getValue()));
                        txtViewFinalGrade.setText((String) dst.child("finalGrade").getValue());
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
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
}