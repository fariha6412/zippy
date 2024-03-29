package com.example.zippy.ui.attendance;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zippy.R;
import com.example.zippy.adapter.AttendanceCustomAdapter;
import com.example.zippy.classes.Course;
import com.example.zippy.helper.MenuHelper;
import com.example.zippy.classes.Student;
import com.example.zippy.utility.NetworkChangeListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RequiresApi(api = Build.VERSION_CODES.O)
public class AttendanceTakingActivity extends AppCompatActivity {
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    SharedPreferences mPrefs;
    final String strClickedCoursePassCode = "clickedCoursePassCode";
    String clickedCoursePassCode;

    private ArrayList<String> studentUIDs;
    private ArrayList<Integer> presentCheckedPositions;
    private Map<String, Boolean> attendance;
    private Map<String, Long> prePresentTotal;
    private Map<String, Long> preAbsentTotal;
    private ArrayList<String> studentNames;
    private ArrayList<String> studentRegistrationNos;
    private AttendanceCustomAdapter adapter;

    private ProgressBar loading;

    private FirebaseDatabase rootNode;
    private DatabaseReference referenceEnrolledStudents;
    private DatabaseReference referenceStudent;
    private DatabaseReference referenceTotal;

    private final LocalDate dateToday = LocalDate.now();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_taking);
        Toolbar toolbar = findViewById(R.id.mToolbar);
        setSupportActionBar(toolbar);
        MenuHelper menuHelper = new MenuHelper(toolbar, this);
        menuHelper.handle();

        Button doneBtn = findViewById(R.id.btnDone);
        loading = findViewById(R.id.loading);
        TextView txtViewDateToday = findViewById(R.id.txtViewDateToday);
        txtViewDateToday.setText(dateToday.toString());

        studentUIDs = new ArrayList<>();
        studentNames = new ArrayList<>();
        studentRegistrationNos = new ArrayList<>();
        presentCheckedPositions = new ArrayList<>();
        attendance = new HashMap<>();
        prePresentTotal = new HashMap<>();
        preAbsentTotal = new HashMap<>();

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        clickedCoursePassCode = mPrefs.getString(strClickedCoursePassCode, "");

        initRecyclerView();
        showList();
        extractPreviousAttendanceRecord();
        doneBtn.setOnClickListener(v -> {
            loading.setVisibility(View.VISIBLE);
            recordAttendance();
            writeToDatabase();
            Toast.makeText(getApplicationContext(), "Done for today.", Toast.LENGTH_SHORT).show();
            loading.setVisibility(View.INVISIBLE);
            finish();
        });
    }

    private void showList() {
        rootNode = FirebaseDatabase.getInstance();
        DatabaseReference referenceCourse = rootNode.getReference("courses/" + clickedCoursePassCode);
        referenceCourse.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Course courseHelper = snapshot.getValue(Course.class);
                if (courseHelper != null) {
                    studentNames.clear();
                    studentUIDs.clear();

                    referenceEnrolledStudents = rootNode.getReference("courses/" + clickedCoursePassCode + "/students");
                    referenceEnrolledStudents.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                            studentUIDs.clear();
                            studentNames.clear();
                            for (DataSnapshot dsnap : dataSnapshot.getChildren()) {
                                System.out.println(dsnap.getValue());

                                String studentUid = (String) dsnap.getValue();
                                referenceStudent = rootNode.getReference("students/" + studentUid);
                                referenceStudent.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull @NotNull DataSnapshot dsnapshot) {
                                        Student studentHelper = dsnapshot.getValue(Student.class);
                                        if (studentHelper != null) {
                                            studentUIDs.add(studentUid);
                                            studentNames.add(studentHelper.getFullName());
                                            studentRegistrationNos.add("RegNo-"+studentHelper.getRegistrationNo());
                                            adapter.notifyDataSetChanged();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
    private void recordAttendance(){
        for (int i = 0; i < studentUIDs.size(); i++){
            if(presentCheckedPositions.contains(i))attendance.put(studentUIDs.get(i), true);
            else attendance.put(studentUIDs.get(i), false);
        }
    }
    private void extractPreviousAttendanceRecord(){
        referenceEnrolledStudents = rootNode.getReference("courses/" + clickedCoursePassCode + "/students");
        referenceEnrolledStudents.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot dsnap:snapshot.getChildren()){
                    String studentUid = (String) dsnap.getValue();
                    referenceTotal = rootNode.getReference("attendanceRecord/total/"+clickedCoursePassCode+"/"+studentUid);
                    referenceTotal.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                Long totalPresent = (Long) dataSnapshot.child("totalPresent").getValue();
                                Long totalAbsent = (Long) dataSnapshot.child("totalAbsent").getValue();
                                prePresentTotal.put(studentUid, totalPresent);
                                preAbsentTotal.put(studentUid, totalAbsent);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
    private void writeToDatabase(){
        DatabaseReference referenceAttendance = rootNode.getReference("attendanceRecord/perDay/" + clickedCoursePassCode);
        referenceAttendance.child(String.valueOf(dateToday)).setValue(attendance, (error, ref) -> System.err.println("Value was set. Error = "+error));
        referenceEnrolledStudents = rootNode.getReference("courses/" + clickedCoursePassCode + "/students");
        referenceEnrolledStudents.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot dsnap:snapshot.getChildren()){
                    String studentUid = (String) dsnap.getValue();
                    referenceTotal = rootNode.getReference("attendanceRecord/total/"+clickedCoursePassCode+"/"+studentUid);
                    Long presentStatus = 0L;
                    Long absentStatus = 0L;
                    if(attendance.get(studentUid))presentStatus = 1L;
                    else absentStatus = 1L;
                    System.out.println(presentStatus);
                    if(prePresentTotal.containsKey(studentUid)){
                        referenceTotal.child("totalPresent").setValue(prePresentTotal.get(studentUid) + presentStatus);
                        referenceTotal.child("totalAbsent").setValue(preAbsentTotal.get(studentUid) + absentStatus);
                    }
                    else{
                        referenceTotal.child("totalPresent").setValue(presentStatus);
                        referenceTotal.child("totalAbsent").setValue(absentStatus);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
    private void initRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new AttendanceCustomAdapter(studentNames, studentRegistrationNos);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        adapter.setOnItemClickListener(position -> {
            //givePresent(position) if not selected previously
            if(presentCheckedPositions.contains(position))presentCheckedPositions.remove(Integer.valueOf(position));
            else presentCheckedPositions.add(position);
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }

    //internet related stuff
    @Override
    protected void onStart() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener, filter);
        super.onStart();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(networkChangeListener);
        super.onStop();
    }
//end stuff
}