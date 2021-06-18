package com.example.zippy.ui.search;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

import com.example.zippy.R;
import com.example.zippy.helper.BottomNavigationHelper;
import com.example.zippy.helper.CourseCustomAdapter;
import com.example.zippy.helper.MenuHelperClass;
import com.example.zippy.helper.SearchCustomAdapter;
import com.example.zippy.helper.StudentHelperClass;
import com.example.zippy.ui.profile.InstructorProfileActivity;
import com.example.zippy.utility.NetworkChangeListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    BottomNavigationView bottomNavigationView;

    SearchView searchView;
    RecyclerView recyclerView;
    SearchCustomAdapter adapter;
    LinearLayoutManager layoutManager;

    FirebaseDatabase rootNode;
    DatabaseReference refStudents, refInstructors, ref;
    ArrayList<String> userFullNameList;
    ArrayList<String> studentsUids;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        bottomNavigationView = findViewById(R.id.bottom_navigatin_view);
        BottomNavigationHelper bottomNavigationHelper = new BottomNavigationHelper(bottomNavigationView, this);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(2);
        menuItem.setChecked(true);
        bottomNavigationHelper.handle();

        searchView = findViewById(R.id.searchview);
        recyclerView = findViewById(R.id.recyclerviewsearch);
        userFullNameList = new ArrayList<>();
        studentsUids = new ArrayList<>();

        rootNode = FirebaseDatabase.getInstance();
        refStudents = rootNode.getReference("students");

        getUserList();
    }
    private void getUserList(){

        refStudents.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for(DataSnapshot dsnap:snapshot.getChildren()){
                    String studentUid = (String) dsnap.getValue();
                    studentsUids.add(studentUid);

                    System.out.println(studentUid);
                    ref = FirebaseDatabase.getInstance().getReference("students/"+studentUid);
                    ref.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                userFullNameList.add(dataSnapshot.getValue(StudentHelperClass.class).getFullName());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                        }
                    });
                }
                System.out.println(userFullNameList.toString());
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
        adapter = new SearchCustomAdapter(userFullNameList);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        adapter.setOnItemClickListener(new SearchCustomAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                //start profile
                }
        });
    }
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }
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
    public void onBackPressed() {
        BottomNavigationHelper.backToProfile(bottomNavigationView, this);
    }
}