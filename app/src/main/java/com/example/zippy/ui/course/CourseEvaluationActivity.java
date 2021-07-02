package com.example.zippy.ui.course;

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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.zippy.R;
import com.example.zippy.helper.MenuHelper;
import com.example.zippy.helper.TestHelper;
import com.example.zippy.ui.attendance.AttendanceDetailsActivity;
import com.example.zippy.ui.profile.ShowCaseUserProfileActivity;
import com.example.zippy.ui.test.TestDetailsActivity;
import com.example.zippy.utility.NetworkChangeListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class CourseEvaluationActivity extends AppCompatActivity {
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    SharedPreferences mPrefs;
    final String strClickedCoursePassCode = "clickedCoursePassCode";
    String clickedCoursePassCode;
    final String strClickedTestId = "clickedTestId";
    String clickedTestId;
    final String strClickedUid = "clickedUid";
    String clickedUid;

    private TextView txtViewTotalPresent, txtViewTotalAbsent, txtViewAttendancePercentage, txtViewFinalGrade;
    private TextView txtViewYourMarkOnAttendance, txtViewYourTotalMark;
    private LinearLayout finalGradeLinearLayout, markOnAttendanceLinearLayout, totalMarkLinearLayout;

    private ArrayList<String> testIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_evaluation);
        Toolbar toolbar = findViewById(R.id.mToolbar);
        setSupportActionBar(toolbar);
        MenuHelper menuHelper = new MenuHelper(toolbar, this);
        menuHelper.handle();

        txtViewTotalPresent = findViewById(R.id.txtviewtotalpresent);
        txtViewTotalAbsent = findViewById(R.id.txtviewtotalabsent);
        txtViewAttendancePercentage = findViewById(R.id.txtviewattendancepercentage);
        txtViewFinalGrade = findViewById(R.id.txtViewFinalGrade);
        txtViewYourMarkOnAttendance = findViewById(R.id.txtViewResultedMarkOnAttendance);
        txtViewYourTotalMark = findViewById(R.id.txtViewTotalMark);
        txtViewFinalGrade = findViewById(R.id.txtViewFinalGrade);
        Button showInstructorProfileBtn = findViewById(R.id.showInstructorProfileBtn);
        LinearLayout attendanceLayout = findViewById(R.id.attendanceLayout);
        markOnAttendanceLinearLayout = findViewById(R.id.resultedMarkOnAttendanceLinearLayout);
        totalMarkLinearLayout = findViewById(R.id.totalMarkLinearLayout);
        finalGradeLinearLayout = findViewById(R.id.finalGradeLinearLayout);
        AutoCompleteTextView autoCompleteTextView = findViewById(R.id.autoCompleteTextView);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        clickedCoursePassCode = mPrefs.getString(strClickedCoursePassCode, "");
        clickedTestId = mPrefs.getString(strClickedTestId, "");
        clickedUid = mPrefs.getString(strClickedUid, "");

        String strIsCompleted = "isCompleted";
        Boolean isCompleted = mPrefs.getBoolean(strIsCompleted, false);

        testIds = new ArrayList<>();
        ArrayList<String> testTitles = new ArrayList<>();

        TestHelper.getTestList(this, testIds, testTitles, autoCompleteTextView, clickedCoursePassCode);
        autoCompleteTextView.setThreshold(0);
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                System.out.println(testIds.get(i));
                mPrefs.edit().putString(strClickedTestId,testIds.get(i)).apply();
                startActivity(new Intent(CourseEvaluationActivity.this, TestDetailsActivity.class));
            }
        });

        if(isCompleted){
            totalMarkLinearLayout.setVisibility(View.VISIBLE);
            finalGradeLinearLayout.setVisibility(View.VISIBLE);
            markOnAttendanceLinearLayout.setVisibility(View.VISIBLE);
            getResultData();
        }

        attendanceLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CourseEvaluationActivity.this, AttendanceDetailsActivity.class));
            }
        });

        showInstructorProfileBtn.setOnClickListener(v -> {
            showInstructorProfile();
        });
        extractData();
    }
    private void extractData(){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        FirebaseDatabase rootNode = FirebaseDatabase.getInstance();
        DatabaseReference referenceAttendance = rootNode.getReference("attendanceRecord/total/" + clickedCoursePassCode + "/" + user.getUid());
        referenceAttendance.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    txtViewTotalPresent.setText(String.valueOf(snapshot.child("totalPresent").getValue()));
                    txtViewTotalAbsent.setText(String.valueOf(snapshot.child("totalAbsent").getValue()));
                    txtViewAttendancePercentage.setText(String.valueOf ((Long.parseLong(String.valueOf(snapshot.child("totalPresent").getValue())) *100.0 )/ (Long.parseLong(String.valueOf(snapshot.child("totalPresent").getValue())) + Long.parseLong(String.valueOf(snapshot.child("totalAbsent").getValue())))));
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
    private void getResultData(){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        assert user != null;
        DatabaseReference referenceResultAttendance = FirebaseDatabase.getInstance().getReference("result/"+user.getUid()+"/"+clickedCoursePassCode+"/"+"attendance/got");
        DatabaseReference referenceResultFinalEvaluation = FirebaseDatabase.getInstance().getReference("result/"+user.getUid()+"/"+clickedCoursePassCode+"/"+"finalEvaluation");
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
    private void showInstructorProfile(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("courses/"+clickedCoursePassCode+"/instructorUID");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                mPrefs.edit().putString(strClickedUid, (String) snapshot.getValue()).apply();
                startActivity(new Intent(CourseEvaluationActivity.this, ShowCaseUserProfileActivity.class));
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
    //end stuff
}