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
import android.widget.Toast;

import com.example.zippy.helper.BottomNavigationHelper;
import com.example.zippy.helper.CourseCustomAdapter;
import com.example.zippy.helper.MenuHelperClass;
import com.example.zippy.helper.SearchCustomAdapter;
import com.example.zippy.R;
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
                    String studentUid = (String) dsnap.getKey();
                    studentsUids.add(studentUid);

                    System.out.println(studentUid);
                    userFullNameList.add(dsnap.getValue(StudentHelperClass.class).getFullName());
                }
                System.out.println(userFullNameList.toString());
                //initRecyclerView();
                if(searchView != null){
                    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String query) {
                            Search(query);
                            return false;
                        }

                        @Override
                        public boolean onQueryTextChange(String newText) {
                            Search(newText);
                            return false;
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Error!", Toast.LENGTH_SHORT).show();
            }

        });
        System.out.println("baire");
    }

    private void initRecyclerView(ArrayList<String> list){
        recyclerView = findViewById(R.id.recyclerviewsearch);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new SearchCustomAdapter(list);
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


    private void Search(String str){

        ArrayList<String> list = new ArrayList<>();
        for(String element : userFullNameList){
            if(element.toLowerCase().contains(str.toLowerCase())){
                list.add(element);
            }
        }
        //SearchCustomAdapter adapter = new SearchCustomAdapter(List);
        //recyclerView.setAdapter(adapter);
        initRecyclerView(list);
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