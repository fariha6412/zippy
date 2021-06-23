package com.example.zippy.ui.course;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.zippy.R;
import com.example.zippy.helper.AttendanceDetailsAdapter;
import com.example.zippy.helper.MenuHelperClass;
import com.example.zippy.helper.TestHelperClass;
import com.example.zippy.ui.attendance.AttendanceDetailsActivity;
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

    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseDatabase rootNode;
    DatabaseReference referenceAttendance;

    private LinearLayout attendanceLayout;
    AutoCompleteTextView autoCompleteTextView;
    private TextView txtViewTotalPresent, txtViewTotalAbsent, txtViewAttendancePercentage, txtViewFinalGrade;

    ArrayAdapter<String> adapter;
    ArrayList<String> testTitles;
    ArrayList<String> testIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_evaluation);
        Toolbar mtoolbar = findViewById(R.id.mtoolbar);
        setSupportActionBar(mtoolbar);
        MenuHelperClass menuHelperClass = new MenuHelperClass(mtoolbar, this);
        menuHelperClass.handle();

        txtViewTotalPresent = findViewById(R.id.txtviewtotalpresent);
        txtViewTotalAbsent = findViewById(R.id.txtviewtotalabsent);
        txtViewAttendancePercentage = findViewById(R.id.txtviewattendancepercentage);
        txtViewFinalGrade = findViewById(R.id.txtviewfinalgrade);
        attendanceLayout = findViewById(R.id.attendanceLayout);
        autoCompleteTextView = findViewById(R.id.autoCompleteTextView);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        clickedCoursePassCode = mPrefs.getString(strClickedCoursePassCode, "");
        clickedTestId = mPrefs.getString(strClickedTestId, "");

        testIds = new ArrayList<>();
        testTitles = new ArrayList<>();

        TestHelperClass.getTestList(this, testIds, testTitles, autoCompleteTextView, clickedCoursePassCode);
        autoCompleteTextView.setThreshold(0);
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                System.out.println(testIds.get(i));
                mPrefs.edit().putString(strClickedTestId,testIds.get(i)).apply();
                startActivity(new Intent(CourseEvaluationActivity.this, TestDetailsActivity.class));
            }
        });

        attendanceLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CourseEvaluationActivity.this, AttendanceDetailsActivity.class));
            }
        });

        extractData();
    }
    private void extractData(){
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        rootNode = FirebaseDatabase.getInstance();
        referenceAttendance = rootNode.getReference("attendanceRecord/total/"+clickedCoursePassCode +"/" +user.getUid());
        referenceAttendance.addListenerForSingleValueEvent(new ValueEventListener() {
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