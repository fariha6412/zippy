package com.example.zippy.ui.course;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zippy.ui.attendance.AttendanceTakingActivity;
import com.example.zippy.R;
import com.example.zippy.helper.MenuHelperClass;
import com.example.zippy.ui.test.TestCreationActivity;
import com.example.zippy.utility.NetworkChangeListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;

@RequiresApi(api = Build.VERSION_CODES.O)
public class CourseDetailsActivity extends AppCompatActivity {
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    FirebaseDatabase rootNode;
    DatabaseReference referenceAttendance;

    SharedPreferences mPrefs;
    final String strClickedCoursePassCode = "clickedCoursePassCode";

    TextView txtViewCoursePassCode;
    Button studentDetailsbtn, attendancebtn;
    MaterialButton testCreationBtn;

    LocalDate datetoday;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_details);

        //new for saving logged user type and clicked course
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String clickedCoursePassCode = mPrefs.getString(strClickedCoursePassCode, "");

        Toolbar mtoolbar = findViewById(R.id.mtoolbar);
        setSupportActionBar(mtoolbar);
        MenuHelperClass menuHelperClass = new MenuHelperClass(mtoolbar, this);
        menuHelperClass.handle();

        datetoday = LocalDate.now();

        txtViewCoursePassCode = findViewById(R.id.txtviewcoursepasscode);
        studentDetailsbtn = findViewById(R.id.studentdetailsbtn);
        attendancebtn = findViewById(R.id.attendance);
        testCreationBtn = findViewById(R.id.testcreationbtn);

        txtViewCoursePassCode.setText(clickedCoursePassCode);

        testCreationBtn.setOnClickListener(v -> {
            startActivity(new Intent(CourseDetailsActivity.this, TestCreationActivity.class));
        });

        studentDetailsbtn.setOnClickListener(v -> {
            startActivity(new Intent(CourseDetailsActivity.this, StudentDetailsActivity.class));
        });
        attendancebtn.setOnClickListener(v -> {
            rootNode = FirebaseDatabase.getInstance();
            referenceAttendance = rootNode.getReference("attendanceRecord/perDay/"+clickedCoursePassCode+"/"+datetoday);
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