package com.example.zippy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zippy.helper.StudentHelperClass;
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

    private EditText editTXTemail, editTXTpassword;
    private ProgressBar loading;
    private FirebaseAuth auth;
    FirebaseDatabase rootNode;
    DatabaseReference referenceStudent, referenceInsturctor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

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
                startActivity(new Intent(MainActivity.this, ChooseAccountType.class));
            }
        });
        txtviewForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText resetMail = new EditText(v.getContext());
                AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext());
                passwordResetDialog.setTitle("Reset Password").setMessage("Enter your email to receive reset link").setCancelable(false) ;
                passwordResetDialog.setView(resetMail);

                passwordResetDialog.setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String email = resetMail.getText().toString();
                        if(email.isEmpty()||!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                            Toast.makeText(getApplicationContext(), "Enter a valid email address", Toast.LENGTH_SHORT).show();
                            //return;
                        };
                        auth.sendPasswordResetEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(getApplicationContext(), "Reset link has been sent!", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull @NotNull Exception e) {
                                Toast.makeText(getApplicationContext(), "Error! Could not sent reset email!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog alert = passwordResetDialog.create();
                alert.show();
            }
        });
        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = editTXTemail.getText().toString().trim();
                String password = editTXTpassword.getText().toString().trim();
                validateEmail(email);
                validatePassword(password);

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
                                    referenceStudent = rootNode.getReference("students/"+user.getUid().toString());
                                    referenceInsturctor = rootNode.getReference("instructors/"+user.getUid().toString());

                                    referenceStudent.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                                            // This method is called once with the initial value and again
                                            // whenever data at this location is updated.
                                            StudentHelperClass value = dataSnapshot.getValue(StudentHelperClass.class);
                                            if(value!=null){
                                                //start the activity of profile of student
                                                //startActivity(new Intent(MainActivity.this, SelfStudentProfileActivity.class));
                                                Log.d("hi", "Value is: " + value);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NotNull DatabaseError error) {
                                            // Failed to read value
                                            Log.w("hhi", "Failed to read value.", error.toException());
                                        }
                                    });

                                    referenceInsturctor.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                                            // This method is called once with the initial value and again
                                            // whenever data at this location is updated.
                                            StudentHelperClass value = dataSnapshot.getValue(StudentHelperClass.class);
                                            if(value!=null){
                                                //start the activity of profile of instructor
                                                //startActivity(new Intent(MainActivity.this, SelfInstructorProfileActivity.class));
                                                //Log.d("hi", "Value is: " + value);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NotNull DatabaseError error) {
                                            // Failed to read value
                                            Log.w("hhi", "Failed to read value.", error.toException());
                                        }
                                    });
                                }
                                else {
                                    Toast.makeText(getApplicationContext(), "Verify your email and try again", Toast.LENGTH_SHORT).show();
                                    editTXTemail.setText("");
                                    editTXTpassword.setText("");
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


    private void validateEmail(String email){
        // if the email input field is empty
        if(email.isEmpty()){
            editTXTemail.setError("Enter an email address");
            editTXTemail.requestFocus();
            return;
        }
        // if the email is valid
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            editTXTemail.setError("Enter a valid email address");
            editTXTemail.requestFocus();
            return;
        }
        editTXTemail.setError(null);
    }

    private void validatePassword(String password){
        // if the password input field is empty
        if(password.isEmpty()){
            editTXTpassword.setError("Enter a password");
            editTXTpassword.requestFocus();
            return;
        }
        editTXTpassword.setError(null);
    }

    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
}