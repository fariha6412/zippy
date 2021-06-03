package com.example.zippy.ui.register;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.zippy.ChooseAccountType;
import com.example.zippy.MainActivity;
import com.example.zippy.R;
import com.example.zippy.helper.StudentHelperClass;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Pattern;

public class RegisterStudentActivity extends AppCompatActivity {

    private EditText editTXTFullName, editTXTEmail, editTXTInstitution;
    private EditText editTXTRegistrationNo, editTXTPassword, editTXTRePassword;
    private ProgressBar loading;
    private FirebaseAuth auth;
    FirebaseDatabase rootNode;
    DatabaseReference reference;
    StudentHelperClass studentHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_student);

        auth = FirebaseAuth.getInstance();

        editTXTFullName = findViewById(R.id.edittxtfullname);
        editTXTEmail = findViewById(R.id.edittxtemail);
        editTXTInstitution = findViewById(R.id.edittxtinstitution);
        editTXTRegistrationNo = findViewById(R.id.edittxtregistrationno);
        editTXTPassword = findViewById(R.id.edittxtpassword);
        editTXTRePassword = findViewById(R.id.edittxtretypepassword);
        loading = findViewById(R.id.loading);
        TextView txtViewLogin = findViewById(R.id.txtviewlogin);
        Button registerbtn = findViewById(R.id.registerbtn);
        String image = "https://firebasestorage.googleapis.com/v0/b/zippy-162e8.appspot.com/o/student.png?alt=media&token=112e1b57-ec64-4992-b24d-8c9aba3c88f7";

        Toolbar mtoolbar = findViewById(R.id.mtoolbar);
        setSupportActionBar(mtoolbar);


        txtViewLogin.setOnClickListener(v -> startActivity(new Intent(RegisterStudentActivity.this, ChooseAccountType.class)));
        registerbtn.setOnClickListener(v -> {
            String email = editTXTEmail.getText().toString().trim();
            String password = editTXTPassword.getText().toString().trim();
            String rePassword = editTXTRePassword.getText().toString().trim();
            String institution = editTXTInstitution.getText().toString().trim();
            String registrationNo = editTXTRegistrationNo.getText().toString().trim();
            String fullName = editTXTFullName.getText().toString().trim();

            if (fullName.isEmpty()) {
                editTXTFullName.setError("Field can not be empty");
                editTXTFullName.requestFocus();
                return;
            }
            if (!validateEmail(email)) return;
            if (institution.isEmpty()) {
                editTXTInstitution.setError("Field can not be empty");
                editTXTInstitution.requestFocus();
                return;
            }
            if (registrationNo.isEmpty()) {
                editTXTRegistrationNo.setError("Field can not be empty");
                editTXTRegistrationNo.requestFocus();
                return;
            }
            if (!validatePassword(password)) return;
            if (rePassword.isEmpty()) {
                editTXTRePassword.setError("Enter password again");
                editTXTRePassword.requestFocus();
                return;
            }
            if (!password.equals(rePassword)) {
                editTXTRePassword.setError("Re-type password does not match");
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
                            reference = rootNode.getReference("students");
                            studentHelper = new StudentHelperClass(image, fullName, email, institution, registrationNo, password);

                            reference.child(user.getUid()).setValue(studentHelper);
                            startActivity(new Intent(RegisterStudentActivity.this, MainActivity.class));
                        }).addOnFailureListener(e -> {
                            loading.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), "Verification Email not sent", Toast.LENGTH_SHORT).show();
                            AuthCredential credential = EmailAuthProvider.getCredential(email, password);

                            user.reauthenticate(credential)
                                    .addOnCompleteListener(task1 -> user.delete()
                                            .addOnCompleteListener(task11 -> {
                                                if(task11.isSuccessful()) {
                                                    //Toast.makeText(RegisterStudentActivity.this, "Deleted User Successfully,", Toast.LENGTH_LONG).show();
                                                }
                                            }));
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
    private boolean validateEmail(String email){
        // if the email input field is empty
        if(email.isEmpty()){
            editTXTEmail.setError("Enter an email address");
            editTXTEmail.requestFocus();
            return false;
        }
        // if the email is valid
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            editTXTEmail.setError("Enter a valid email address");
            editTXTEmail.requestFocus();
            return false;
        }
        editTXTEmail.setError(null);
        return true;
    }

    private boolean validatePassword(String password){
        // if the password input field is empty
        if(password.isEmpty()){
            editTXTPassword.setError("Enter a password");
            editTXTPassword.requestFocus();
            return false;
        }
        Pattern PASSWORD_PATTERN =
                Pattern.compile("^" +
                        "(?=.*[@#$%^&+=])" +     // at least 1 special character
                        "(?=\\S+$)" +            // no white spaces
                        ".{6,}" +                // at least 4 characters
                        "$");

        // if password does not matches to the pattern
        // it will display an error message "Password is too weak"
        if(!PASSWORD_PATTERN.matcher(password).matches()) {
            editTXTPassword.setError("Password is too weak\nRules:\n1.No white spaces\n"
                                +"2.At least six characters\n3.At least one special character");
            return false;
        }
        editTXTPassword.setError(null);
        return  true;
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
}