package com.example.zippy.ui.profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.zippy.AboutActivity;
import com.example.zippy.CourseCreationActivity;
import com.example.zippy.CourseDetailsActivity;
import com.example.zippy.MainActivity;
import com.example.zippy.R;
import com.example.zippy.helper.CourseCustomAdapter;
import com.example.zippy.helper.CourseHelperClass;
import com.example.zippy.helper.InstructorHelperClass;
import com.example.zippy.helper.MenuHelperClass;
import com.example.zippy.helper.StudentHelperClass;
import com.example.zippy.ui.change.ChangeProfilePictureActivity;
import com.example.zippy.ui.register.RegisterStudentActivity;
import com.example.zippy.utility.NetworkChangeListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class InstructorProfileActivity extends AppCompatActivity implements AdapterView.OnClickListener {

    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    FirebaseAuth auth;
    FirebaseDatabase rootNode;
    DatabaseReference reference, referenceCourseList, referenceCourse;
    FirebaseUser user;

    TextView txtViewFullName, txtViewInstitution, txtViewDesignation, txtViewEmplyeeID;
    ImageView img;
    MaterialButton createbtn;

    RecyclerView recyclerView;
    CourseCustomAdapter adapter;
    LinearLayoutManager layoutManager;
    List<CourseHelperClass> courseList = new ArrayList<CourseHelperClass>();
    Long noOfCourses = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructor_profile);
        showProfile();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menuabout:
                MenuHelperClass.showAbout(this);
                return true;
            case R.id.menuexit:
                MenuHelperClass.exit(this);
                return true;
            case R.id.menulogout:
                MenuHelperClass.signOut(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }
    public void showProfile(){
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        txtViewFullName = findViewById(R.id.txtviewfullname);
        txtViewInstitution = findViewById(R.id.txtviewinstitution);
        txtViewDesignation = findViewById(R.id.txtviewdesignation);
        txtViewEmplyeeID = findViewById(R.id.txtviewemployeeid);
        img = findViewById(R.id.imgview);
        createbtn = findViewById(R.id.createbtn);

        rootNode = FirebaseDatabase.getInstance();
        reference = rootNode.getReference("instructors/"+ user.getUid());

        Toolbar mtoolbar = findViewById(R.id.mtoolbar);

        setSupportActionBar(mtoolbar);

        createbtn.setOnClickListener(v -> {
            startActivity(new Intent(InstructorProfileActivity.this, CourseCreationActivity.class));
        });

        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                InstructorHelperClass value = dataSnapshot.getValue(InstructorHelperClass.class);
                if(value!=null){
                    txtViewFullName.setText(value.getFullName());
                    txtViewDesignation.setText(value.getDesignation());
                    txtViewEmplyeeID.setText("EmployeeID: "+value.getEmployeeID());
                    txtViewInstitution.setText(value.getInstitution());

                    Glide.with(getBaseContext()).load(value.getImage()).into(img);
                    noOfCourses = value.getNoOfCourses();
                    Log.d("Response", "Value is: " + value.toString());

                    courseList.clear();
                    referenceCourseList = rootNode.getReference("instructors/"+user.getUid()+"/courses");
                    referenceCourseList.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            for(DataSnapshot dsnap:snapshot.getChildren()){
                                String coursePassCode = (String) dsnap.getValue();
                                courseList.clear();
                                referenceCourse = rootNode.getReference("courses/"+coursePassCode);
                                referenceCourse.addValueEventListener(new ValueEventListener() {
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
                                        Log.w("Error", "Failed to read value.", error.toException());
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {
                            Log.w("Error", "Failed to read value.", error.toException());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NotNull DatabaseError error) {
                // Failed to read value
                Log.w("Error", "Failed to read value.", error.toException());
            }
        });
    }
    private void initRecyclerView(){
        recyclerView = findViewById(R.id.recylerview);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new CourseCustomAdapter(courseList, this::onClick);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
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
    public void onClick(View v) {
        startActivity(new Intent(InstructorProfileActivity.this, CourseDetailsActivity.class));
    }
    //end stuff
}