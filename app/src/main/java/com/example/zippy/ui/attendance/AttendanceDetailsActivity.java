package com.example.zippy.ui.attendance;

import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zippy.R;
import com.example.zippy.adapter.AttendanceDetailsAdapter;
import com.example.zippy.helper.MenuHelper;
import com.example.zippy.utility.NetworkChangeListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AttendanceDetailsActivity extends AppCompatActivity {
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    SharedPreferences mPrefs;
    final String strClickedCoursePassCode = "clickedCoursePassCode";
    String clickedCoursePassCode;

    private Map<String, Boolean> attendance;
    private ArrayList<String> dates;
    private ArrayList<String> presentStatus;
    private AttendanceDetailsAdapter adapter;

    private TextView txtViewTotalDay;

    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_details);

        Toolbar toolbar = findViewById(R.id.mToolbar);
        setSupportActionBar(toolbar);
        MenuHelper menuHelper = new MenuHelper(toolbar, this);
        menuHelper.handle();

        attendance = new HashMap<>();
        dates = new ArrayList<>();
        presentStatus = new ArrayList<>();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        txtViewTotalDay = findViewById(R.id.txtViewTotalDay);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        clickedCoursePassCode = mPrefs.getString(strClickedCoursePassCode, "");

        initRecyclerView();
        extractAttendanceRecord();
    }
    private void extractAttendanceRecord(){
        FirebaseDatabase rootNode = FirebaseDatabase.getInstance();
        DatabaseReference referenceAttendance = rootNode.getReference("attendanceRecord/perDay/" + clickedCoursePassCode);
        referenceAttendance.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for(DataSnapshot dsnap:snapshot.getChildren()){
                    String date = dsnap.getKey();
                    try {
                        attendance = (Map<String, Boolean>) dsnap.getValue();
                        dates.add(date);
                        if(attendance.get(user.getUid()) != null){
                            if(attendance.get(user.getUid())){
                                presentStatus.add("Present");
                            }
                            else {
                                presentStatus.add("Absent");
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                    catch (Exception e){
                        Log.w("error: ", e.getMessage());
                    }
                }
                adapter.notifyDataSetChanged();
                txtViewTotalDay.setText(String.valueOf(snapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void initRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new AttendanceDetailsAdapter(dates, presentStatus);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }

    //internet related stuff
    @Override
    protected void onStart() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener, filter);
        super.onStart();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(networkChangeListener);
        super.onStop();
    }
}