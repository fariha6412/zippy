package com.example.zippy.ui.profile;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.zippy.classes.Instructor;
import com.example.zippy.classes.Student;
import com.example.zippy.ui.course.CourseCreationActivity;
import com.example.zippy.ui.course.CourseDetailsActivity;
import com.example.zippy.ui.course.CourseEnrollActivity;
import com.example.zippy.ui.course.CourseEvaluationActivity;
import com.example.zippy.R;
import com.example.zippy.helper.BottomNavigationHelper;
import com.example.zippy.adapter.CourseCustomAdapter;
import com.example.zippy.classes.Course;
import com.example.zippy.helper.MenuHelper;
import com.example.zippy.utility.NetworkChangeListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class UserProfileActivity extends AppCompatActivity{
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    private SharedPreferences mPrefs;
    final String strClickedCoursePassCode = "clickedCoursePassCode";
    final String loggedStatus = "loggedProfile";
    private String loggedProfile;

    private FirebaseDatabase rootNode;
    private DatabaseReference referenceCourse, reference;
    private DatabaseReference referenceCourseList;
    private FirebaseUser user;

    private TextView txtViewFullName, txtViewInstitution, txtViewRegistrationNo;
    private TextView txtViewDesignation, txtViewEmployeeId;
    private ImageView img;

    private final ArrayList<Course> courseList = new ArrayList<>();
    private final ArrayList<Boolean> courseCompletionStatus = new ArrayList<>();
    private CourseCustomAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        FirebaseMessaging.getInstance().subscribeToTopic("all");

        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if(user==null)finish();
        TextView txtViewCourseHeader = findViewById(R.id.txtViewCourseHeader);
        txtViewFullName = findViewById(R.id.txtViewFullName);
        txtViewInstitution = findViewById(R.id.txtViewInstitution);
        txtViewRegistrationNo = findViewById(R.id.txtViewRegistrationNo);
        txtViewDesignation = findViewById(R.id.txtViewDesignation);
        txtViewEmployeeId = findViewById(R.id.txtViewEmployeeId);
        img = findViewById(R.id.imgView);
        MaterialButton addBtn = findViewById(R.id.addBtn);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        loggedProfile = mPrefs.getString(loggedStatus, "nouser");

        rootNode = FirebaseDatabase.getInstance();

        Toolbar toolbar = findViewById(R.id.mToolbar);
        setSupportActionBar(toolbar);
        MenuHelper menuHelper = new MenuHelper(toolbar, this);
        menuHelper.handle();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        BottomNavigationHelper bottomNavigationHelper = new BottomNavigationHelper(bottomNavigationView, this);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(0);
        menuItem.setChecked(true);
        bottomNavigationHelper.handle();
        addBtn.setOnClickListener(v -> {
            if(loggedProfile.equals("student")) {
                startActivity(new Intent(UserProfileActivity.this, CourseEnrollActivity.class));
            }
            else {
                startActivity(new Intent(UserProfileActivity.this, CourseCreationActivity.class));
            }
        });

        initRecyclerView();
        if(loggedProfile.equals("student")){
            reference = rootNode.getReference("students/" + user.getUid());
            txtViewRegistrationNo.setVisibility(View.VISIBLE);
            showStudentProfile();
        }
        else if(loggedProfile.equals("instructor")){
            txtViewCourseHeader.setText(R.string.str_created_courses);
            reference = rootNode.getReference("instructors/" + user.getUid());
            txtViewDesignation.setVisibility(View.VISIBLE);
            txtViewEmployeeId.setVisibility(View.VISIBLE);
            showInstructorProfile();
        }

    }
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }
    public void showStudentProfile(){
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Student value = dataSnapshot.getValue(Student.class);
                if(value!=null){
                    txtViewFullName.setText(value.getFullName());
                    txtViewRegistrationNo.setText("RegistrationNO: "+value.getRegistrationNo());
                    txtViewInstitution.setText(value.getInstitution());

                    Glide.with(getBaseContext()).load(value.getImage()).into(img);
                    //Log.d("Response", "Value is: " + value.toString());
                    courseList.clear();
                    courseCompletionStatus.clear();
                    referenceCourseList = rootNode.getReference("students/"+user.getUid()+"/courses");
                    showCourseList();
                }
            }

            @Override
            public void onCancelled(@NotNull DatabaseError error) {
                // Failed to read value
                //Log.w("Error", "Failed to read value.", error.toException());
            }
        });
    }
    public void showInstructorProfile(){
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Instructor value = dataSnapshot.getValue(Instructor.class);
                if(value!=null){
                    txtViewFullName.setText(value.getFullName());
                    txtViewDesignation.setText(value.getDesignation());
                    txtViewEmployeeId.setText("EmployeeID: "+value.getEmployeeID());
                    txtViewInstitution.setText(value.getInstitution());

                    Glide.with(getBaseContext()).load(value.getImage()).into(img);
                    //Log.d("Response", "Value is: " + value.toString());

                    courseList.clear();
                    courseCompletionStatus.clear();
                    referenceCourseList = rootNode.getReference("instructors/"+user.getUid()+"/courses");
                    showCourseList();
                }
            }

            @Override
            public void onCancelled(@NotNull DatabaseError error) {
                // Failed to read value
                //Log.w("Error", "Failed to read value.", error.toException());
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
                    courseCompletionStatus.clear();
                    referenceCourse = rootNode.getReference("courses/"+coursePassCode);
                    referenceCourse.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            Boolean isCompleted = (Boolean) snapshot.child("isCompleted").getValue();

                            if(isCompleted != null && isCompleted)courseCompletionStatus.add(true);
                            else courseCompletionStatus.add(false);
                            //System.out.println(courseCompletionStatus.size()+" "+ courseList.size());
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
    private void initRecyclerView(){
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new CourseCustomAdapter(courseList, courseCompletionStatus);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        adapter.setOnItemClickListener(new CourseCustomAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                String strIsCompleted = "isCompleted";
                String coursePassCode = courseList.get(position).getCoursePassCode();
                mPrefs.edit().putString(strClickedCoursePassCode,coursePassCode).apply();
                mPrefs.edit().putBoolean(strIsCompleted, courseCompletionStatus.get(position)).apply();
                if(loggedProfile.equals("student"))startActivity(new Intent(UserProfileActivity.this, CourseEvaluationActivity.class));
                else startActivity(new Intent(UserProfileActivity.this, CourseDetailsActivity.class));
            }
        });
    }
    @Override
    public void onBackPressed() {
        MenuHelper.exit(this);
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