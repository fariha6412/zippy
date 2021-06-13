package com.example.zippy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.zippy.helper.CourseCustomAdapter;
import com.example.zippy.helper.CourseHelperClass;
import com.example.zippy.helper.MenuHelperClass;
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

    private ArrayList<String> studentUids;
    private ArrayList<String> studentNames;

    private TextView txtViewTotalStudent;

    FirebaseDatabase rootNode;
    DatabaseReference referenceEnrolledStudents, referenceCourse, referenceStudent;

    RecyclerView recyclerView;
    StudentCustomAdapter adapter;
    LinearLayoutManager layoutManager;
    Long noOfStudents = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_details);
        Toolbar mtoolbar = findViewById(R.id.mtoolbar);
        setSupportActionBar(mtoolbar);

        studentUids = new ArrayList<>();
        studentNames = new ArrayList<String>();
        txtViewTotalStudent = findViewById(R.id.txtviewtotalstudent);

        //new for saving logged user type and clicked course
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        clickedCoursePassCode = mPrefs.getString(strClickedCoursePassCode, "");

        System.out.println(clickedCoursePassCode);
        showlist();
    }
    private void showlist(){
        rootNode = FirebaseDatabase.getInstance();
        referenceCourse = rootNode.getReference("courses/"+clickedCoursePassCode);
        referenceCourse.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                CourseHelperClass courseHelper = snapshot.getValue(CourseHelperClass.class);
                if(courseHelper!=null){
                    studentNames.clear();
                    studentUids.clear();
                    noOfStudents = courseHelper.getNoOfStudents();
                    txtViewTotalStudent.setText(noOfStudents.toString());

                    referenceEnrolledStudents = rootNode.getReference("courses/"+clickedCoursePassCode+"/students");
                    referenceEnrolledStudents.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                            for(DataSnapshot dsnap:dataSnapshot.getChildren()){
                                System.out.println(dsnap.getValue());

                                String studentUid = (String) dsnap.getValue();
                                referenceStudent = rootNode.getReference("students/"+studentUid);
                                referenceStudent.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull @NotNull DataSnapshot dsnapshot) {
                                        StudentHelperClass studentHelper = dsnapshot.getValue(StudentHelperClass.class);
                                        if(studentHelper!=null){
                                            studentUids.add(studentUid);
                                            studentNames.add(studentHelper.getFullName());
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
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
    private void initRecyclerView(){
        recyclerView = findViewById(R.id.recylerview);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new StudentCustomAdapter(studentNames);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        adapter.setOnItemClickListener(new StudentCustomAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                //show profile of the student
            }
            @Override
            public void onDeleteClick(int position) {
                //delete showing a alert dialog
                //removeItem(position);
            }
        });
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