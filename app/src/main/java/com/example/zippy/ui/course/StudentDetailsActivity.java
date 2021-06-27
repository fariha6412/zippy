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
import android.widget.TextView;

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

public class StudentDetailsActivity extends AppCompatActivity {
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    SharedPreferences mPrefs;
    final String strClickedCoursePassCode = "clickedCoursePassCode";
    String clickedCoursePassCode;
    final String strClickedUid = "clickedUid";
    String clickedUid;
    private String courseTitle;

    private ArrayList<String> studentUIDs;
    private ArrayList<String> studentNames;
    private ArrayList<String> studentRegistrationNos;

    private TextView txtViewTotalStudent;

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

        //new for saving logged user type and clicked course
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        clickedCoursePassCode = mPrefs.getString(strClickedCoursePassCode, "");
        clickedUid = mPrefs.getString(strClickedUid, "");

        System.out.println(clickedCoursePassCode);
        showList();
    }
    private void showList(){
        FirebaseDatabase rootNode = FirebaseDatabase.getInstance();
        DatabaseReference referenceCourseNoOfStudents = rootNode.getReference("courses/" + clickedCoursePassCode +"/noOfStudents");
        referenceCourseNoOfStudents.addListenerForSingleValueEvent(new ValueEventListener() {
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
                for(DataSnapshot dsnap:dataSnapshot.getChildren()){
                    System.out.println(dsnap.getValue());

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
                                initRecyclerView();
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
        StudentCustomAdapter adapter = new StudentCustomAdapter(studentNames, studentRegistrationNos);
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
                //delete showing a alert dialog
                new AlertDialog.Builder(StudentDetailsActivity.this)
                        .setTitle("Message")
                        .setMessage("Do you want to remove " + studentNames.get(position) + "?")
                        .setNegativeButton("NO", null)
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NotificationHelper.notifyUser(studentUIDs.get(position), "Sorry", "you have been removed from " + courseTitle,StudentDetailsActivity.this);
                                unrollStudent(position);
                            }
                        }).create().show();
            }
        });
    }

    private void unrollStudent(int position){
        //
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