package com.example.zippy.ui.profile;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.zippy.helper.NotificationHelper;
import com.example.zippy.ui.course.CourseEnrollActivity;
import com.example.zippy.ui.course.CourseEvaluationActivity;
import com.example.zippy.R;
import com.example.zippy.helper.BottomNavigationHelper;
import com.example.zippy.helper.CourseCustomAdapter;
import com.example.zippy.helper.CourseHelperClass;
import com.example.zippy.helper.MenuHelperClass;
import com.example.zippy.helper.StudentHelperClass;
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

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class StudentProfileActivity extends AppCompatActivity{
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    private SharedPreferences mPrefs;
    final String strClickedCoursePassCode = "clickedCoursePassCode";

    private FirebaseDatabase rootNode;
    private DatabaseReference referenceCourse;
    private DatabaseReference referenceCourseList;
    private FirebaseUser user;

    private TextView txtViewFullName, txtViewInstitution, txtViewRegistrationNo;
    private ImageView img;

    private final ArrayList<CourseHelperClass> courseList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_profile);
        showProfile();
    }
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }
    public void showProfile(){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if(user==null)finish();
        txtViewFullName = findViewById(R.id.txtviewfullname);
        txtViewInstitution = findViewById(R.id.txtviewinstitution);
        txtViewRegistrationNo = findViewById(R.id.txtviewregistraionno);
        img = findViewById(R.id.imgview);
        MaterialButton addBtn = findViewById(R.id.addbtn);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        rootNode = FirebaseDatabase.getInstance();
        DatabaseReference referenceStudent = rootNode.getReference("students/" + user.getUid());

        Toolbar mtoolbar = findViewById(R.id.mtoolbar);
        setSupportActionBar(mtoolbar);
        MenuHelperClass menuHelperClass = new MenuHelperClass(mtoolbar, this);
        menuHelperClass.handle();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigatin_view);
        //bottomNavigationView.performClick();
        BottomNavigationHelper bottomNavigationHelper = new BottomNavigationHelper(bottomNavigationView, this);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(0);
        menuItem.setChecked(true);
        bottomNavigationHelper.handle();
        //bottomNavigationView.setSelectedItemId(R.id.navigation_profile);

        referenceStudent.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                StudentHelperClass value = dataSnapshot.getValue(StudentHelperClass.class);
                if(value!=null){
                    txtViewFullName.setText(value.getFullName());
                    txtViewRegistrationNo.setText("RegistrationNO: "+value.getRegistrationNo());
                    txtViewInstitution.setText(value.getInstitution());

                    Glide.with(getBaseContext()).load(value.getImage()).into(img);
                    Log.d("Response", "Value is: " + value.toString());
                    courseList.clear();
                    referenceCourseList = rootNode.getReference("students/"+user.getUid()+"/courses");
                    referenceCourseList.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            for(DataSnapshot dsnap:snapshot.getChildren()){
                                String coursePassCode = (String) dsnap.getValue();
                                courseList.clear();
                                referenceCourse = rootNode.getReference("courses/"+coursePassCode);
                                referenceCourse.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                        CourseHelperClass courseHelper = snapshot.getValue(CourseHelperClass.class);
                                        if(courseHelper!=null) {
                                            courseList.add(courseHelper);
                                            initRecyclerView();
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
            }

            @Override
            public void onCancelled(@NotNull DatabaseError error) {
                // Failed to read value
                //Log.w("Error", "Failed to read value.", error.toException());
            }
        });
        addBtn.setOnClickListener(v -> {
            startActivity(new Intent(StudentProfileActivity.this, CourseEnrollActivity.class));
        });
    }
    private void initRecyclerView(){
        RecyclerView recyclerView = findViewById(R.id.recylerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        CourseCustomAdapter adapter = new CourseCustomAdapter(courseList);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        adapter.setOnItemClickListener(new CourseCustomAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                //intent courseEvaluation
                String clickedCoursePassCode = mPrefs.getString(strClickedCoursePassCode, "");
                String coursePassCode = courseList.get(position).getCoursePassCode();
                mPrefs.edit().putString(strClickedCoursePassCode,coursePassCode).apply();
                //System.out.println(clickedCoursePassCode+" "+ coursePassCode);
                //intent courseEvaluation
                startActivity(new Intent(StudentProfileActivity.this, CourseEvaluationActivity.class));

            }
        });
    }
    @Override
    public void onBackPressed() {
        MenuHelperClass.exit(this);
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
    @Override
    protected void onResume() {
        new Thread( new Runnable(){
            @Override
            public void run(){
                NotificationHelper.testMarkGivenNotification(user, StudentProfileActivity.this);
            }
        }).start();
        super.onResume();
    }
    //end stuff
}