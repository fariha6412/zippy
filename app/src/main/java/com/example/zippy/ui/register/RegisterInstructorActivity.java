package com.example.zippy.ui.register;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.zippy.MainActivity;
import com.example.zippy.R;
import com.example.zippy.helper.InstructorHelperClass;
import com.example.zippy.helper.ValidationChecker;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

public class RegisterInstructorActivity extends AppCompatActivity {

    private EditText editTXTFullName, editTXTEmail, editTXTInstitution;
    private EditText editTXTEmployeeID, editTXTPassword, editTXTRePassword;
    private EditText editTXTDesignation;
    private ProgressBar loading;
    FirebaseAuth auth;
    FirebaseDatabase rootNode;
    DatabaseReference reference;
    InstructorHelperClass instructorHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_instructor);

        auth = FirebaseAuth.getInstance();

        editTXTFullName = findViewById(R.id.edittxtfullname);
        editTXTEmail = findViewById(R.id.edittxtemail);
        editTXTInstitution = findViewById(R.id.edittxtinstitution);
        editTXTEmployeeID = findViewById(R.id.edittxtemplyeeid);
        editTXTDesignation = findViewById(R.id.edittxtdesignation);
        editTXTPassword = findViewById(R.id.edittxtpassword);
        editTXTRePassword = findViewById(R.id.edittxtretypepassword);
        loading = findViewById(R.id.loading);
        TextView txtViewLogin = findViewById(R.id.txtviewlogin);
        Button registerbtn = findViewById(R.id.registerbtn);
        String image = "https://firebasestorage.googleapis.com/v0/b/zippy-162e8.appspot.com/o/instructor.png?alt=media&token=7d594bc1-2490-4a36-b75f-765966a5ce82";

        Toolbar mtoolbar = findViewById(R.id.mtoolbar);
        setSupportActionBar(mtoolbar);

        txtViewLogin.setOnClickListener(v -> startActivity(new Intent(RegisterInstructorActivity.this, MainActivity.class)));
        registerbtn.setOnClickListener(v -> {
            String email = editTXTEmail.getText().toString().trim();
            String password = editTXTPassword.getText().toString().trim();
            String rePassword = editTXTRePassword.getText().toString().trim();
            String institution = editTXTInstitution.getText().toString().trim();
            String employeeId = editTXTEmployeeID.getText().toString().trim();
            String designation = editTXTDesignation.getText().toString().trim();
            String fullName = editTXTFullName.getText().toString().trim();

            if(ValidationChecker.isFieldEmpty(fullName, editTXTFullName))return;
            if(!ValidationChecker.isValidEmail(email, editTXTEmail))return;
            if(ValidationChecker.isFieldEmpty(institution, editTXTInstitution))return;
            if(ValidationChecker.isFieldEmpty(employeeId, editTXTEmployeeID))return;
            if(ValidationChecker.isFieldEmpty(designation, editTXTDesignation))return;
            if(!ValidationChecker.isValidPassword(password, editTXTPassword))return;
            if(ValidationChecker.isFieldEmpty(rePassword, editTXTRePassword))return;
            if(!password.equals(rePassword)){
                editTXTRePassword.setError("Re-typed password does not match");
                editTXTRePassword.requestFocus();
                return;
            }
            loading.setVisibility(View.VISIBLE);
            //user creation
            try {
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        loading.setVisibility(View.GONE);
                        FirebaseUser user = auth.getCurrentUser();
                        assert user != null;
                        //email verification
                        user.sendEmailVerification().addOnSuccessListener(unused -> {
                            loading.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), "Verification Email has been sent", Toast.LENGTH_SHORT).show();
                            rootNode = FirebaseDatabase.getInstance();
                            reference = rootNode.getReference("instructors");
                            instructorHelper = new InstructorHelperClass(image, fullName, email, institution, employeeId, designation);

                            reference.child(user.getUid()).setValue(instructorHelper,new DatabaseReference.CompletionListener(){

                                @Override
                                public void onComplete(@Nullable @org.jetbrains.annotations.Nullable DatabaseError error, @NonNull @NotNull DatabaseReference ref) {
                                    System.err.println("Value was set. Error = "+error);
                                }
                            });
                            startActivity(new Intent(RegisterInstructorActivity.this, MainActivity.class));
                        }).addOnFailureListener(e -> {
                            loading.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), "Verification Email not sent", Toast.LENGTH_SHORT).show();
                            AuthCredential credential = EmailAuthProvider.getCredential(email, password);

                            // Prompt the user to re-provide their sign-in credentials
                            user.reauthenticate(credential)
                                    .addOnCompleteListener(task1 -> //Toast.makeText(RegisterInstructor.this, "Deleted User Successfully,", Toast.LENGTH_LONG).show();
                                            user.delete()
                                            .addOnCompleteListener(Task::isSuccessful));
                        });
                    } else {
                        Toast.makeText(getApplicationContext(), "Could not Register", Toast.LENGTH_SHORT).show();
                        loading.setVisibility(View.GONE);
                    }
                });
            }
            catch (Exception e){
                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
}