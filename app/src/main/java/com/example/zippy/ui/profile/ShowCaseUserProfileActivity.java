package com.example.zippy.ui.profile;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.zippy.R;
import com.example.zippy.adapter.CourseCustomAdapter;
import com.example.zippy.classes.Course;
import com.example.zippy.classes.Instructor;
import com.example.zippy.helper.EmailSender;
import com.example.zippy.helper.MenuHelper;
import com.example.zippy.classes.Student;
import com.example.zippy.ui.chat.ChatActivity;
import com.example.zippy.utility.NetworkChangeListener;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
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

public class ShowCaseUserProfileActivity extends AppCompatActivity {
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    SharedPreferences mPrefs;
    final String loggedStatus = "loggedProfile";
    final String strClickedUid = "clickedUid";
    private String clickedUid;
    private String loggedProfile;

    private ImageView imgView;
    private TextView txtViewProfileLocked, txtViewFullName, txtViewEmail, txtViewInstitution, txtViewDesignation, txtViewEmployeeID, txtViewRegistrationNo, txtViewCourseHeader;
    private LinearLayout designationLinearLayout, employeeIDLinearLayout, registrationNoLinearLayout;
    private LinearLayout courseListHeaderLinearLayout;

    private FirebaseUser user;
    private FirebaseDatabase rootNode;
    private DatabaseReference referenceStudent, referenceInstructor, referenceCourse, referenceCourseList;

    private RecyclerView recyclerView;
    private final ArrayList<Course> courseList = new ArrayList<>();
    private final ArrayList<Boolean> courseCompletionStatus = new ArrayList<>();
    private CourseCustomAdapter adapter;

    private Boolean isFabHidden;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_case_user_profile);

        Toolbar toolbar = findViewById(R.id.mToolbar);
        setSupportActionBar(toolbar);
        MenuHelper menuHelper = new MenuHelper(toolbar, this);
        menuHelper.handle();

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        clickedUid = mPrefs.getString(strClickedUid, "");
        loggedProfile = mPrefs.getString(loggedStatus, "nouser");

        imgView = findViewById(R.id.imgView);

        FloatingActionButton sendMail = findViewById(R.id.email_fab);
        FloatingActionButton sendMessage = findViewById(R.id.message_fab);
        ExtendedFloatingActionButton contact = findViewById(R.id.contact_fab);

        txtViewProfileLocked = findViewById(R.id.txtviewprofileloced);
        txtViewFullName = findViewById(R.id.txtViewFullName);
        txtViewEmail = findViewById(R.id.txtviewemail);
        txtViewDesignation = findViewById(R.id.txtViewDesignation);
        txtViewEmployeeID = findViewById(R.id.txtViewEmployeeId);
        txtViewRegistrationNo = findViewById(R.id.txtViewRegistrationNo);
        txtViewInstitution = findViewById(R.id.txtViewInstitution);
        txtViewCourseHeader = findViewById(R.id.txtViewCourseHeader);
        recyclerView = findViewById(R.id.recyclerView);

        designationLinearLayout = findViewById(R.id.designationlinearlayout);
        registrationNoLinearLayout = findViewById(R.id.registrationnolinearlayout);
        employeeIDLinearLayout = findViewById(R.id.employeeidlinearlayout);
        courseListHeaderLinearLayout = findViewById(R.id.courselistheaderlinearlayout);

        sendMail.setVisibility(View.GONE);
        sendMessage.setVisibility(View.GONE);

        isFabHidden = true;
        contact.shrink();

        initRecyclerView();
        showData();

        contact.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (isFabHidden) {
                            sendMail.show();
                            sendMessage.show();
                            contact.extend();
                            isFabHidden = false;
                        } else {
                            sendMail.hide();
                            sendMessage.hide();
                            contact.shrink();
                            isFabHidden = true;
                        }
                    }
                });
        sendMail.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EmailSender.sendEmailTo(ShowCaseUserProfileActivity.this, new String[] {txtViewEmail.getText().toString().trim()});
                    }
                });
        sendMessage.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                         startActivity(new Intent(ShowCaseUserProfileActivity.this, ChatActivity.class));

                    }
                });
    }

    private void showData(){
        rootNode = FirebaseDatabase.getInstance();
        referenceInstructor = rootNode.getReference("instructors/"+clickedUid);
        referenceStudent = rootNode.getReference("students/"+clickedUid);

        referenceStudent.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Student studentHelper = snapshot.getValue(Student.class);
                    assert studentHelper != null;
                    txtViewFullName.setText(studentHelper.getFullName());
                    txtViewInstitution.setText(studentHelper.getInstitution());
                    txtViewEmail.setText(studentHelper.getEmail());

                    registrationNoLinearLayout.setVisibility(View.VISIBLE);
                    txtViewRegistrationNo.setText(studentHelper.getRegistrationNo());
                    Glide.with(getBaseContext()).load(studentHelper.getImage()).into(imgView);

                    if(Objects.equals(snapshot.child("isProfileLocked").getValue(), true)){
                        txtViewProfileLocked.setVisibility(View.VISIBLE);
                        if(loggedProfile.equals("instructor")){
                            System.out.println(loggedProfile);
                            courseListHeaderLinearLayout.setVisibility(View.VISIBLE);
                            txtViewCourseHeader.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.VISIBLE);
                            showOnlyUsersCourse();
                        }
                    }
                    else{
                        referenceCourseList = referenceStudent.child("courses");
                        courseListHeaderLinearLayout.setVisibility(View.VISIBLE);
                        txtViewCourseHeader.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.VISIBLE);
                        showCourseList();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
        referenceInstructor.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Instructor instructorHelper = snapshot.getValue(Instructor.class);

                    assert instructorHelper != null;
                    txtViewFullName.setText(instructorHelper.getFullName());
                    txtViewEmail.setText(instructorHelper.getEmail());
                    txtViewInstitution.setText(instructorHelper.getInstitution());
                    txtViewDesignation.setText(instructorHelper.getDesignation());
                    txtViewEmployeeID.setText(instructorHelper.getEmployeeID());
                    employeeIDLinearLayout.setVisibility(View.VISIBLE);
                    designationLinearLayout.setVisibility(View.VISIBLE);
                    txtViewCourseHeader.setVisibility(View.VISIBLE);
                    txtViewCourseHeader.setText(R.string.str_created_courses);
                    courseListHeaderLinearLayout.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.VISIBLE);
                    Glide.with(getBaseContext()).load(instructorHelper.getImage()).into(imgView);

                    referenceCourseList = referenceInstructor.child("courses");
                    showCourseList();
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
    private void showCourseList(){
        referenceCourseList.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for(DataSnapshot dsnap:snapshot.getChildren()){
                    String coursePassCode = (String) dsnap.getValue();
                    courseList.clear();
                    referenceCourse = rootNode.getReference("courses/"+coursePassCode);
                    referenceCourse.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            Boolean isCompleted = (Boolean) snapshot.child("isCompleted").getValue();

                            if(isCompleted != null && isCompleted)courseCompletionStatus.add(true);
                            else courseCompletionStatus.add(false);
                            Course courseHelper = snapshot.getValue(Course.class);
                            if(courseHelper!=null) {
                                courseList.add(courseHelper);
                                adapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {
                            //Log.w("Error", "Failed to read value.", error.toException());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                //Log.w("Error", "Failed to read value.", error.toException());
            }
        });
    }
    private void showOnlyUsersCourse(){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        referenceCourseList = referenceStudent.child("courses");
        referenceCourseList.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for(DataSnapshot dsnap:snapshot.getChildren()){
                    String coursePassCode = (String) dsnap.getValue();
                    courseList.clear();
                    referenceCourse = rootNode.getReference("courses/"+coursePassCode);
                    referenceCourse.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapt) {

                            Course courseHelper = snapt.getValue(Course.class);
                            if(courseHelper!=null) {
                                System.out.println(user.getUid());
                                System.out.println(courseHelper.getInstructorUID());
                                if(courseHelper.getInstructorUID().equals(user.getUid())){
                                    Boolean isCompleted = (Boolean) snapt.child("isCompleted").getValue();
                                    if(isCompleted != null && isCompleted)courseCompletionStatus.add(true);
                                    else courseCompletionStatus.add(false);
                                    courseList.add(courseHelper);
                                }
                                adapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {
                            //Log.w("Error", "Failed to read value.", error.toException());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                //Log.w("Error", "Failed to read value.", error.toException());
            }
        });
    }
    private void initRecyclerView(){
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new CourseCustomAdapter(courseList, courseCompletionStatus);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
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