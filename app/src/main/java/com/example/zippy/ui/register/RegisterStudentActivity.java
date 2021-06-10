package com.example.zippy.ui.register;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.zippy.AboutActivity;
import com.example.zippy.ChooseAccountTypeActivity;
import com.example.zippy.MainActivity;
import com.example.zippy.R;
import com.example.zippy.helper.MenuHelperClass;
import com.example.zippy.helper.StudentHelperClass;
import com.example.zippy.helper.ValidationChecker;
import com.example.zippy.ui.change.ChangeProfilePictureActivity;
import com.example.zippy.ui.profile.StudentProfileActivity;
import com.example.zippy.utility.NetworkChangeListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public class RegisterStudentActivity extends AppCompatActivity {
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

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

        txtViewLogin.setOnClickListener(v -> startActivity(new Intent(RegisterStudentActivity.this, ChooseAccountTypeActivity.class)));
        registerbtn.setOnClickListener(v -> {
            String email = editTXTEmail.getText().toString().trim();
            String password = editTXTPassword.getText().toString().trim();
            String rePassword = editTXTRePassword.getText().toString().trim();
            String institution = editTXTInstitution.getText().toString().trim();
            String registrationNo = editTXTRegistrationNo.getText().toString().trim();
            String fullName = editTXTFullName.getText().toString().trim();

            if(ValidationChecker.isFieldEmpty(fullName, editTXTFullName))return;
            if(!ValidationChecker.isValidEmail(email, editTXTEmail))return;
            if(ValidationChecker.isFieldEmpty(institution, editTXTInstitution))return;
            if(ValidationChecker.isFieldEmpty(registrationNo, editTXTRegistrationNo))return;
            if(!ValidationChecker.isValidPassword(password, editTXTPassword))return;
            if(ValidationChecker.isFieldEmpty(rePassword, editTXTRePassword))return;
            if (!password.equals(rePassword)) {
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
                            reference = rootNode.getReference("students");
                            studentHelper = new StudentHelperClass(image, fullName, email, institution, registrationNo);

                            reference.child(user.getUid()).setValue(studentHelper,new DatabaseReference.CompletionListener(){

                                @Override
                                public void onComplete(@Nullable @org.jetbrains.annotations.Nullable DatabaseError error, @NonNull @NotNull DatabaseReference ref) {
                                    System.err.println("Value was set. Error = "+error);
                                }
                            });
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
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
}