package com.example.zippy.ui.course;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zippy.R;
import com.example.zippy.helper.CourseHelperClass;
import com.example.zippy.helper.FcmNotificationsSender;
import com.example.zippy.helper.MenuHelperClass;
import com.example.zippy.helper.NotificationHelper;
import com.example.zippy.helper.StudentCustomAdapter;
import com.example.zippy.helper.StudentHelperClass;
import com.example.zippy.utility.NetworkChangeListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

public class StudentDetailsActivity extends AppCompatActivity {
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    SharedPreferences mPrefs;
    final String strClickedCoursePassCode = "clickedCoursePassCode";
    String clickedCoursePassCode;
    final String strClickedUid = "clickedUid";
    final String strIsCompleted = "isCompleted";
    private Boolean isCompleted;

    String clickedUid;
    private String courseTitle;

    private ArrayList<String> studentUIDs;
    private ArrayList<String> studentNames;
    private ArrayList<String> studentRegistrationNos;

    private TextView txtViewTotalStudent;
    private StudentCustomAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_details);
        Toolbar toolbar = findViewById(R.id.mToolbar);
        setSupportActionBar(toolbar);
        MenuHelperClass menuHelperClass = new MenuHelperClass(toolbar, this);
        menuHelperClass.handle();

        studentUIDs = new ArrayList<>();
        studentNames = new ArrayList<>();
        studentRegistrationNos = new ArrayList<>();
        txtViewTotalStudent = findViewById(R.id.txtviewtotalstudent);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        clickedCoursePassCode = mPrefs.getString(strClickedCoursePassCode, "");
        clickedUid = mPrefs.getString(strClickedUid, "");
        isCompleted = mPrefs.getBoolean(strIsCompleted, false);

        System.out.println(clickedCoursePassCode);
        showNoOfStudents();
        showList();
        initRecyclerView();

    }
    private void showNoOfStudents(){
        FirebaseDatabase rootNode = FirebaseDatabase.getInstance();
        DatabaseReference referenceCourseNoOfStudents = rootNode.getReference("courses/" + clickedCoursePassCode +"/noOfStudents");
        referenceCourseNoOfStudents.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    txtViewTotalStudent.setText(snapshot.getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
    private void showList(){
        FirebaseDatabase rootNode = FirebaseDatabase.getInstance();
        DatabaseReference referenceCourseTitle = rootNode.getReference("courses/" + clickedCoursePassCode +"/courseTitle");
        referenceCourseTitle.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    courseTitle = snapshot.getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
        DatabaseReference referenceEnrolledStudents = rootNode.getReference("courses/"+clickedCoursePassCode+"/students");
        referenceEnrolledStudents.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                studentUIDs.clear();
                studentNames.clear();
                studentRegistrationNos.clear();
                for(DataSnapshot dsnap:dataSnapshot.getChildren()){
                    System.out.println(dsnap.getValue());
                    studentUIDs.clear();
                    studentNames.clear();
                    studentRegistrationNos.clear();
                    String studentUid = (String) dsnap.getValue();
                    DatabaseReference referenceStudent = rootNode.getReference("students/"+studentUid);
                    referenceStudent.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot dsnapshot) {
                            StudentHelperClass studentHelper = dsnapshot.getValue(StudentHelperClass.class);
                            if(studentHelper!=null){
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
    private void initRecyclerView(){
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new StudentCustomAdapter(studentNames, studentRegistrationNos);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        adapter.setOnItemClickListener(new StudentCustomAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(int position) {
                //show profile of the student
                mPrefs.edit().putString(strClickedUid, studentUIDs.get(position)).apply();
                startActivity(new Intent(StudentDetailsActivity.this, AfterStudentDetailsActivity.class));
            }
            @Override
            public void onDeleteClick(int position) {
                if(isCompleted){
                    Toast.makeText(StudentDetailsActivity.this, "Course is completed", Toast.LENGTH_SHORT).show();
                    return;
                }
                //delete showing a alert dialog
                new AlertDialog.Builder(StudentDetailsActivity.this)
                        .setTitle("Message")
                        .setMessage("Do you want to remove " + studentNames.get(position) + "?")
                        .setNeutralButton("Block", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                unrollStudent(position, true);
                            }
                        })
                        .setNegativeButton("NO", null)
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NotificationHelper.notifyUser(studentUIDs.get(position), "Sorry", "you have been removed from " + courseTitle,StudentDetailsActivity.this);
                                unrollStudent(position, false);
                            }
                        }).create().show();
            }
        });
    }

    private void unrollStudent(int position, Boolean block){
        String uid = studentUIDs.get(position);
        FirebaseDatabase rootNode = FirebaseDatabase.getInstance();
        DatabaseReference referenceCourse = rootNode.getReference("courses/"+clickedCoursePassCode);
        DatabaseReference referenceStudent = rootNode.getReference("students/"+ uid);


        referenceCourse.child("noOfStudents").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Long noOfStudents = (Long) snapshot.getValue();
                    referenceCourse.child("noOfStudents").setValue(noOfStudents - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
        referenceStudent.child("courses").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for(DataSnapshot dsnap:snapshot.getChildren()){
                    System.out.println(dsnap.getValue());
                    if(clickedCoursePassCode.equals((String)dsnap.getValue())){

                        referenceStudent.child("courses").child(Objects.requireNonNull(dsnap.getKey())).removeValue();
                        referenceCourse.child("students").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                for(DataSnapshot dsnp:snapshot.getChildren()){
                                    if(uid.equals((String)dsnp.getValue())){
                                        referenceCourse.child("students").child(dsnp.getKey()).removeValue();
                                    }
                                    studentNames.remove(position);
                                    studentRegistrationNos.remove(position);
                                    studentUIDs.remove(position);
                                    adapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
        referenceStudent.child("noOfCourses").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Long noOfCourses = (Long) snapshot.getValue();
                    referenceStudent.child("noOfCourses").setValue(noOfCourses - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
        if(block)referenceCourse.child("blockedStudents").child(uid).setValue(true);
        //else referenceCourse.child("unrolledStudents").push().setValue(uid);
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