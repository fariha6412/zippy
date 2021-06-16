package com.example.zippy;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.zippy.helper.CourseHelperClass;
import com.example.zippy.helper.InstructorHelperClass;
import com.example.zippy.helper.MenuHelperClass;
import com.example.zippy.helper.StudentHelperClass;
import com.example.zippy.helper.ValidationChecker;
import com.example.zippy.ui.profile.InstructorProfileActivity;
import com.example.zippy.ui.profile.StudentProfileActivity;
import com.example.zippy.utility.NetworkChangeListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity {

    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    private EditText editTXTemail, editTXTpassword;
    private ProgressBar loading;
    private FirebaseAuth auth;
    FirebaseDatabase rootNode;
    DatabaseReference referenceStudent, referenceInstructor;

    ////new changes for splash
    SharedPreferences mPrefs;
    final String splashScreenPref = "SplashScreenShown";
    final String loggedStatus = "loggedProfile";
    ////done

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        ////new changes for splash
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean splashScreenShown = mPrefs.getBoolean(splashScreenPref, false);
        if (!splashScreenShown) {
            Intent intent = new Intent(MainActivity.this,SplashActivity.class);
            startActivity(intent);

            mPrefs.edit().putBoolean(splashScreenPref, true).apply();
            finish();
        }

        String loggedProfile = mPrefs.getString(loggedStatus, "nouser");
        if(user!=null){
            if(loggedProfile.equals("instructor")){
                startActivity(new Intent(MainActivity.this, InstructorProfileActivity.class));
                finish();
            }
            if(loggedProfile.equals("student")){
                startActivity(new Intent(MainActivity.this, StudentProfileActivity.class));
                finish();
            }
        }
        ////done

        TextView txtViewRegister = findViewById(R.id.txtviewregister);
        editTXTemail = findViewById(R.id.edittxtemail);
        editTXTpassword = findViewById(R.id.edittxtpassword);
        Button loginbtn = findViewById(R.id.btnlogin);
        TextView txtviewForgotPassword = findViewById(R.id.txtviewforgotpassword);
        loading = findViewById(R.id.loading);
        Toolbar mtoolbar = findViewById(R.id.mtoolbar);

        setSupportActionBar(mtoolbar);

        txtViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ChooseAccountTypeActivity.class));
            }
        });
        txtviewForgotPassword.setOnClickListener(new View.OnClickListener() {
            Boolean wantToCloseDialog = false;
            String email = "";

            @Override
            public void onClick(View v) {
                EditText resetMail = new EditText(v.getContext());
                AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext());
                passwordResetDialog.setTitle("Reset Password").setMessage("Enter your email to receive reset link").setCancelable(false) ;
                passwordResetDialog.setView(resetMail);

                DialogInterface.OnClickListener yesBtnFunc = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        wantToCloseDialog = false;
                    }

                };
                DialogInterface.OnClickListener noBtnFunc = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        wantToCloseDialog = true;
                    }
                };
                passwordResetDialog.setPositiveButton("yes", yesBtnFunc);
                passwordResetDialog.setNegativeButton("No", noBtnFunc);
                AlertDialog alert = passwordResetDialog.create();
                alert.show();

                alert.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        //Do stuff.
                        email = resetMail.getText().toString().trim();
                        if(ValidationChecker.isFieldEmpty(email, resetMail)){
                            return;
                        }
                        if(!ValidationChecker.isValidEmail(email, resetMail)) {
                            return;
                        }
                        auth.sendPasswordResetEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(getApplicationContext(), "Reset link has been sent!", Toast.LENGTH_SHORT).show();
                                alert.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull @NotNull Exception e) {
                                Toast.makeText(getApplicationContext(), "Error! Could not sent reset email!", Toast.LENGTH_SHORT).show();
                                alert.dismiss();
                            }
                        });

                        if(wantToCloseDialog) {
                            alert.dismiss();
                        }
                    }
                });
                alert.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        //Do stuff, possibly set wantToCloseDialog to true then...
                        alert.dismiss();
                    }
                });
            }
        });
        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = editTXTemail.getText().toString().trim();
                String password = editTXTpassword.getText().toString().trim();
                if(!ValidationChecker.isValidEmail(email, editTXTemail))return;
                if(!ValidationChecker.isValidPassword(password, editTXTpassword))return;

                loading.setVisibility(View.VISIBLE);
                try {
                    auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                loading.setVisibility(View.GONE);
                                FirebaseUser user = auth.getCurrentUser();
                                assert user != null;
                                if(user.isEmailVerified()){
                                    rootNode = FirebaseDatabase.getInstance();
                                    referenceStudent = rootNode.getReference("students/"+ user.getUid());
                                    referenceInstructor = rootNode.getReference("instructors/"+ user.getUid());

                                    referenceStudent.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                                            // This method is called once with the initial value and again
                                            // whenever data at this location is updated.
                                            StudentHelperClass value = dataSnapshot.getValue(StudentHelperClass.class);
                                            if(value!=null){
                                                //start the activity of profile of student
                                                mPrefs.edit().putString(loggedStatus,"student").apply();
                                                loading.setVisibility(View.GONE);
                                                startActivity(new Intent(MainActivity.this, StudentProfileActivity.class));
                                                Log.d("Response", "Value is: " + value.toString());
                                                //finish();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NotNull DatabaseError error) {
                                            // Failed to read value
                                            //Log.w("Error", "Failed to read value.", error.toException());
                                        }
                                    });

                                    referenceInstructor.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                                            // This method is called once with the initial value and again
                                            // whenever data at this location is updated.
                                            InstructorHelperClass value = dataSnapshot.getValue(InstructorHelperClass.class);
                                            if(value!=null){
                                                //start the activity of profile of instructor
                                                mPrefs.edit().putString(loggedStatus,"instructor").apply();
                                                loading.setVisibility(View.GONE);
                                                startActivity(new Intent(MainActivity.this, InstructorProfileActivity.class));
                                                Log.d("Response", "Value is: " + value.toString());
                                                //finish();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NotNull DatabaseError error) {
                                            // Failed to read value
                                            //Log.w("Error", "Failed to read value.", error.toException());
                                            loading.setVisibility(View.GONE);
                                        }
                                    });
                                }
                                else {
                                    Toast.makeText(getApplicationContext(), "Verify your email and try again", Toast.LENGTH_SHORT).show();
                                    editTXTemail.setText("");
                                    editTXTpassword.setText("");
                                    loading.setVisibility(View.GONE);
                                }
                            }
                            else{
                                Toast.makeText(getApplicationContext(), "Could not log in", Toast.LENGTH_SHORT).show();
                                editTXTemail.setText("");
                                editTXTpassword.setText("");
                                loading.setVisibility(View.GONE);
                            }
                        }
                    });
                }
                catch (Exception e){
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
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
            default:
                return super.onOptionsItemSelected(item);
        }
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

    @Override
    public void onBackPressed() {
        MenuHelperClass.exit(this);
    }
}