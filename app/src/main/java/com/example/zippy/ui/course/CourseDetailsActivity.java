package com.example.zippy.ui.course;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.zippy.R;
import com.example.zippy.helper.MenuHelperClass;
import com.example.zippy.helper.TestHelperClass;
import com.example.zippy.ui.attendance.AttendanceTakingActivity;
import com.example.zippy.ui.test.TestCreationActivity;
import com.example.zippy.ui.test.TestDetailsActivity;
import com.example.zippy.utility.NetworkChangeListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.ArrayList;

@RequiresApi(api = Build.VERSION_CODES.O)
public class CourseDetailsActivity extends AppCompatActivity {
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    private FirebaseDatabase rootNode;
    private DatabaseReference referenceAttendance;

    private SharedPreferences mPrefs;
    final String strClickedCoursePassCode = "clickedCoursePassCode";
    String clickedCoursePassCode;
    final String strClickedTestId = "clickedTestId";
    String clickedTestId;

    private ArrayList<String> testIds;

    private LocalDate dateToday;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_details);

        //new for saving logged user type and clicked course
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        clickedCoursePassCode = mPrefs.getString(strClickedCoursePassCode, "");
        clickedTestId = mPrefs.getString(strClickedTestId, "");

        Toolbar mtoolbar = findViewById(R.id.mtoolbar);
        setSupportActionBar(mtoolbar);
        MenuHelperClass menuHelperClass = new MenuHelperClass(mtoolbar, this);
        menuHelperClass.handle();

        dateToday = LocalDate.now();

        TextView txtViewCoursePassCode = findViewById(R.id.txtviewcoursepasscode);
        Button studentDetailsBtn = findViewById(R.id.studentdetailsbtn);
        Button attendanceBtn = findViewById(R.id.attendance);
        MaterialButton testCreationBtn = findViewById(R.id.testcreationbtn);
        AutoCompleteTextView autoCompleteTextView = findViewById(R.id.autoCompleteTextView);

        testIds = new ArrayList<>();
        ArrayList<String> testTitles = new ArrayList<>();

        TestHelperClass.getTestList(this, testIds, testTitles, autoCompleteTextView, clickedCoursePassCode);
        autoCompleteTextView.setThreshold(0);
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                System.out.println(testIds.get(i));
                mPrefs.edit().putString(strClickedTestId,testIds.get(i)).apply();
                startActivity(new Intent(CourseDetailsActivity.this, TestDetailsActivity.class));
            }
        });

        txtViewCoursePassCode.setText(clickedCoursePassCode);

        testCreationBtn.setOnClickListener(v -> {
            startActivity(new Intent(CourseDetailsActivity.this, TestCreationActivity.class));
        });

        studentDetailsBtn.setOnClickListener(v -> {
            startActivity(new Intent(CourseDetailsActivity.this, StudentDetailsActivity.class));
        });
        attendanceBtn.setOnClickListener(v -> {
            rootNode = FirebaseDatabase.getInstance();
            referenceAttendance = rootNode.getReference("attendanceRecord/perDay/"+clickedCoursePassCode+"/"+ dateToday);
            referenceAttendance.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        Toast.makeText(getApplicationContext(), "Attendance already taken for today.", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        startActivity(new Intent(CourseDetailsActivity.this, AttendanceTakingActivity.class));
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
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