package com.example.zippy.ui.profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.zippy.R;
import com.example.zippy.helper.InstructorHelperClass;
import com.example.zippy.helper.MenuHelperClass;
import com.example.zippy.helper.StudentHelperClass;
import com.example.zippy.utility.NetworkChangeListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

public class CommonUserProfileActivity extends AppCompatActivity {
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    SharedPreferences mPrefs;
    final String strClickedUid = "clickedUid";
    String clickedUid;

    private ImageView imgView;
    private FloatingActionButton floatingActionButton;
    private TextView txtView;


    FirebaseDatabase rootNode;
    DatabaseReference referenceStudent, referenceInstructor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_user_profile);

        Toolbar mtoolbar = findViewById(R.id.mtoolbar);
        setSupportActionBar(mtoolbar);
        MenuHelperClass menuHelperClass = new MenuHelperClass(mtoolbar, this);
        menuHelperClass.handle();

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        clickedUid = mPrefs.getString(strClickedUid, "");

        imgView = findViewById(R.id.imgview);
        floatingActionButton = findViewById(R.id.chatbtn);
        txtView = findViewById(R.id.txtview);
        showData();
    }

    private void showData(){
        rootNode = FirebaseDatabase.getInstance();
        referenceInstructor = rootNode.getReference("instructors/"+clickedUid);
        referenceStudent = rootNode.getReference("students/"+clickedUid);

        referenceStudent.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    StudentHelperClass studentHelper = snapshot.getValue(StudentHelperClass.class);
                    txtView.setText(studentHelper.showProfileDetails());
                    Glide.with(getBaseContext()).load(studentHelper.getImage()).into(imgView);
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
                    InstructorHelperClass instructorHelper = snapshot.getValue(InstructorHelperClass.class);
                    txtView.setText(instructorHelper.showProfileDetails());
                    Glide.with(getBaseContext()).load(instructorHelper.getImage()).into(imgView);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
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