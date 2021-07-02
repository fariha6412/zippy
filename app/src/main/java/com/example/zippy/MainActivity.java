package com.example.zippy;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.example.zippy.classes.Instructor;
import com.example.zippy.classes.Student;
import com.example.zippy.helper.MenuHelper;
import com.example.zippy.helper.MyFirebaseMessagingService;
import com.example.zippy.helper.ValidationChecker;
import com.example.zippy.ui.extras.SplashActivity;
import com.example.zippy.ui.profile.UserProfileActivity;
import com.example.zippy.ui.register.ChooseAccountTypeActivity;
import com.example.zippy.utility.NetworkChangeListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
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

    private TextInputEditText editTXTEmail, editTXTPassword;
    private FirebaseAuth auth;
    private FirebaseDatabase rootNode;
    private DatabaseReference referenceStudent, referenceInstructor;

    private SharedPreferences mPrefs;
    final String splashScreenPref = "SplashScreenShown";
    final String loggedStatus = "loggedProfile";
    final String darkThemeStatus = "darkTheme";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean splashScreenShown = mPrefs.getBoolean(splashScreenPref, false);
        if (!splashScreenShown) {
            Intent intent = new Intent(MainActivity.this, SplashActivity.class);
            startActivity(intent);

            mPrefs.edit().putBoolean(splashScreenPref, true).apply();
            finish();
        }

        boolean darkTheme = mPrefs.getBoolean(darkThemeStatus, false);
        if(darkTheme){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        if(user!=null){
            startActivity(new Intent(this, UserProfileActivity.class));
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TextView txtViewRegister = findViewById(R.id.txtviewregister);
        editTXTEmail = findViewById(R.id.edittxtemail);
        editTXTPassword = findViewById(R.id.edittxtpassword);
        Button loginBtn = findViewById(R.id.btnlogin);
        ImageView imgView = findViewById(R.id.imgView);
        if(darkTheme){
            imgView.setVisibility(View.GONE);
        }
        else{
            imgView.setVisibility(View.VISIBLE);
        }
        TextView txtViewForgotPassword = findViewById(R.id.txtviewforgotpassword);
        Toolbar toolbar = findViewById(R.id.mToolbar);

        setSupportActionBar(toolbar);

        txtViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ChooseAccountTypeActivity.class));
            }
        });
        txtViewForgotPassword.setOnClickListener(new View.OnClickListener() {
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
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = editTXTEmail.getText().toString().trim();
                String password = editTXTPassword.getText().toString().trim();
                if(!ValidationChecker.isValidEmail(email, editTXTEmail))return;
                if(!ValidationChecker.isValidPassword(password, editTXTPassword))return;

                ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setTitle("Please wait");
                progressDialog.setMessage("Logging in");
                progressDialog.show();
                try {
                    auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                progressDialog.dismiss();
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
                                            Student value = dataSnapshot.getValue(Student.class);
                                            if(value!=null){
                                                saveToken(user.getUid());
                                                //start the activity of profile of student
                                                mPrefs.edit().putString(loggedStatus,"student").apply();
                                                progressDialog.dismiss();
                                                startActivity(new Intent(MainActivity.this, UserProfileActivity.class));
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
                                            Instructor value = dataSnapshot.getValue(Instructor.class);
                                            if(value!=null){
                                                //start the activity of profile of instructor
                                                saveToken(user.getUid());
                                                mPrefs.edit().putString(loggedStatus,"instructor").apply();
                                                progressDialog.dismiss();
                                                startActivity(new Intent(MainActivity.this, UserProfileActivity.class));
                                                Log.d("Response", "Value is: " + value.toString());
                                                //finish();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NotNull DatabaseError error) {
                                            // Failed to read value
                                            //Log.w("Error", "Failed to read value.", error.toException());
                                            progressDialog.dismiss();
                                        }
                                    });
                                }
                                else {
                                    Toast.makeText(getApplicationContext(), "Verify your email and try again", Toast.LENGTH_SHORT).show();
                                    editTXTEmail.setText("");
                                    editTXTPassword.setText("");
                                    progressDialog.dismiss();
                                }
                            }
                            else{
                                Toast.makeText(getApplicationContext(), "Could not log in", Toast.LENGTH_SHORT).show();
                                editTXTEmail.setText("");
                                editTXTPassword.setText("");
                                progressDialog.dismiss();
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

    private void saveToken(String userUID){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("userTokens");
        reference.child(userUID).setValue(MyFirebaseMessagingService.getToken(MainActivity.this), new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable @org.jetbrains.annotations.Nullable DatabaseError error, @NonNull @NotNull DatabaseReference ref) {
                //Toast.makeText(MainActivity.this, "token saved", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menuabout:
                MenuHelper.showAbout(this);
                return true;
            case R.id.menuexit:
                MenuHelper.exit(this);
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
        MenuHelper.exit(this);
    }
}