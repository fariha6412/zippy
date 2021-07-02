package com.example.zippy.ui.setting;

import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.zippy.R;
import com.example.zippy.helper.MenuHelper;
import com.example.zippy.utility.NetworkChangeListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

public class ChangeInstructorProfileInfoActivity extends AppCompatActivity {
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    private Button updatebtn;
    FirebaseStorage storage;
    StorageReference storageReference;
    DatabaseReference reference;
    FirebaseAuth mAuth;
    FirebaseUser user;
    SharedPreferences sp;
    private EditText edtTXTusername, edtTXTinstitute, edtTXTdesignation, edtTXTemployid;
    String _NAME, _INSTITUTION, _DESIGNATION, _EMPLOYID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_instructor_profile_info);
        updatebtn = findViewById(R.id.updatebtn);
        edtTXTusername = findViewById(R.id.edittxtchangeInstructorusername);
        edtTXTdesignation = findViewById(R.id.edittxtchangedesignation);
        edtTXTemployid = findViewById(R.id.edittxtchangeemployid);
        edtTXTinstitute = findViewById(R.id.edittxtchangeInstructorinstitute);
        //mAuth = FirebaseAuth.getInstance();
         user = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("instructors/" + user.getUid() );
        sp = getApplicationContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        _NAME = sp.getString("fullName", "");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        //reference = FirebaseDatabase.getInstance().getReference("instructors").child(_NAME);


        Toolbar toolbar = findViewById(R.id.mToolbar);
        setSupportActionBar(toolbar);
        MenuHelper menuHelper = new MenuHelper(toolbar, this);
        menuHelper.handle();
        showAllUserData();
        updatebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateinfo();
            }
        });


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
    //end stuff

    private void showAllUserData() {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                _NAME = snapshot.child("fullName").getValue(String.class);
                _EMPLOYID=snapshot.child("employeeID").getValue(String.class);
                _INSTITUTION = snapshot.child("institution").getValue(String.class);
                _DESIGNATION = snapshot.child("designation").getValue(String.class);


                edtTXTusername.setText(_NAME);
                edtTXTdesignation.setText(_DESIGNATION);
                edtTXTinstitute.setText(_INSTITUTION);
                edtTXTemployid.setText(_EMPLOYID);
                /*if(loadURL.length() > 1)
                    Picasso.get().load(loadURL).into(showImg);*/
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
//        System.out.println(_NAME);
//        Intent intent = getIntent();
//        _NAME = intent.getStringExtra("fullName");
//        _DESIGNATION = intent.getStringExtra("designation");
//        _INSTITUTION = intent.getStringExtra("institution");
//        _EMPLOYID = intent.getStringExtra("employID");
//
//        edtTXTusername.setText(_NAME);
//        edtTXTdesignation.setText(_DESIGNATION);
//        edtTXTinstitute.setText(_INSTITUTION);
//        edtTXTemployid.setText(_EMPLOYID);

    }

    public void updateinfo() {
        if (isNameChanged() || isisInstituteChanged() || isDesignationChanged() || isEmployIdChanged()) {
            Toast.makeText(ChangeInstructorProfileInfoActivity.this, "Data has been updated", Toast.LENGTH_LONG).show();
            finish();
        } else
            Toast.makeText(ChangeInstructorProfileInfoActivity.this, "Data is same and can not be updated", Toast.LENGTH_LONG).show();

    }

    private boolean isEmployIdChanged() {
        if (!_EMPLOYID.equals(edtTXTemployid.getText().toString())) {
            reference.child("employeeID").setValue(edtTXTemployid.getText().toString());
            return true;
        } else {
            return false;
        }
    }

    private boolean isDesignationChanged() {
        if (!_DESIGNATION.equals(edtTXTdesignation.getText().toString())) {
            reference.child("designation").setValue(edtTXTdesignation.getText().toString());
            return true;
        } else {
            return false;
        }
    }

    private boolean isisInstituteChanged() {
        if (!_INSTITUTION.equals(edtTXTinstitute.getText().toString())) {
            reference.child("institution").setValue(edtTXTinstitute.getText().toString());
            return true;
        } else {
            return false;
        }
    }

    private boolean isNameChanged() {
        if (!_NAME.equals(edtTXTusername.getText().toString())) {
            reference.child("fullName").setValue(edtTXTusername.getText().toString());
            return true;
        } else {
            return false;
        }
    }
}

