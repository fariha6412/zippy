package com.example.zippy.ui.course;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.example.zippy.R;
import com.example.zippy.helper.BlockListCustomAdapter;
import com.example.zippy.helper.CourseCustomAdapter;
import com.example.zippy.helper.MenuHelperClass;
import com.example.zippy.helper.NotificationHelper;
import com.example.zippy.ui.profile.ShowCaseUserProfileActivity;
import com.example.zippy.ui.profile.UserProfileActivity;
import com.example.zippy.ui.search.SearchActivity;
import com.example.zippy.utility.NetworkChangeListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

public class BlockListActivity extends AppCompatActivity {
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    private SharedPreferences mPrefs;
    final String strClickedUid = "clickedUid";
    final String strClickedCoursePassCode = "clickedCoursePassCode";
    private String clickedCoursePassCode;
    private Boolean isCompleted;

    private String courseTitle;
    private BlockListCustomAdapter adapter;
    private ArrayList<String> studentNameList, uidList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_block_list);
        studentNameList = new ArrayList<>();
        uidList = new ArrayList<>();
        initRecyclerView();

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String strIsCompleted = "isCompleted";
        isCompleted = mPrefs.getBoolean(strIsCompleted, false);
        clickedCoursePassCode = mPrefs.getString(strClickedCoursePassCode, "");

        showData();
    }
    private void showData(){
        DatabaseReference referenceCourseTitle = FirebaseDatabase.getInstance().getReference("courses/" + clickedCoursePassCode +"/courseTitle");
        referenceCourseTitle.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    courseTitle = Objects.requireNonNull(snapshot.getValue()).toString();
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
        FirebaseDatabase.getInstance().getReference("courses/"+clickedCoursePassCode+"/blockedStudents").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                studentNameList.clear();
                uidList.clear();
                for(DataSnapshot dsnap:snapshot.getChildren()){
                    String uid = (String) dsnap.getKey();
                    uidList.add(uid);
                    FirebaseDatabase.getInstance().getReference("students/"+uid+"/fullName").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot spshot) {
                            if(spshot.exists()){
                                studentNameList.add((String) spshot.getValue());
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
        adapter = new BlockListCustomAdapter(studentNameList);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        adapter.setOnItemClickListener(new BlockListCustomAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                //intent showCaseUserProfile
                mPrefs.edit().putString(strClickedUid, uidList.get(position)).apply();
                startActivity(new Intent(BlockListActivity.this, ShowCaseUserProfileActivity.class));
            }

            @Override
            public void onDeleteClick(int position) {
                //unBlock User and remove from list
                if(isCompleted){
                    Toast.makeText(BlockListActivity.this, "Course is Completed", Toast.LENGTH_SHORT).show();
                }
                else{
                    unblock(position);
                }
            }

        });
    }
    private void unblock(int position){
        String uid = uidList.get(position);
        FirebaseDatabase.getInstance().getReference("courses/"+clickedCoursePassCode+"/blockedStudents").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for(DataSnapshot dsnap:snapshot.getChildren()){
                    if(uid.equals((String) dsnap.getKey())){
                        FirebaseDatabase.getInstance().getReference("courses/"+clickedCoursePassCode+"/blockedStudents").child((String)dsnap.getKey()).removeValue();
                        NotificationHelper.notifyUser(uid, "Hurray", "You have been unblocked from " +courseTitle, BlockListActivity.this);
                        Toast.makeText(BlockListActivity.this, "Unblocking successful", Toast.LENGTH_SHORT).show();
                        uidList.remove(position);
                        studentNameList.remove(position);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
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