package com.example.zippy.ui.register;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.zippy.MainActivity;
import com.example.zippy.R;
import com.example.zippy.classes.Student;
import com.example.zippy.helper.MenuHelper;
import com.example.zippy.helper.ValidationChecker;
import com.example.zippy.utility.NetworkChangeListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterStudentActivity extends AppCompatActivity {
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    SharedPreferences mPrefs;
    final String loggedStatus = "loggedProfile";

    private EditText editTXTFullName, editTXTEmail, editTXTInstitution;
    private EditText editTXTRegistrationNo, editTXTPassword, editTXTRePassword;
    private ProgressBar loading;
    private FirebaseAuth auth;
    private FirebaseDatabase rootNode;
    private DatabaseReference reference;
    private Student studentHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_student);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        auth = FirebaseAuth.getInstance();

        editTXTFullName = findViewById(R.id.edittxtfullname);
        editTXTEmail = findViewById(R.id.edittxtemail);
        editTXTInstitution = findViewById(R.id.edittxtinstitution);
        editTXTRegistrationNo = findViewById(R.id.edittxtregistrationno);
        editTXTPassword = findViewById(R.id.edittxtpassword);
        editTXTRePassword = findViewById(R.id.edittxtretypepassword);
        loading = findViewById(R.id.loading);
        TextView txtViewLogin = findViewById(R.id.txtviewlogin);
        Button registerBtn = findViewById(R.id.registerbtn);
        String image = "https://firebasestorage.googleapis.com/v0/b/zippy-162e8.appspot.com/o/student.png?alt=media&token=784d27b3-e5ad-4601-93e5-6ab1e2b51b4f";

        Toolbar toolbar = findViewById(R.id.mToolbar);
        setSupportActionBar(toolbar);

        txtViewLogin.setOnClickListener(v -> startActivity(new Intent(RegisterStudentActivity.this, ChooseAccountTypeActivity.class)));
        registerBtn.setOnClickListener(v -> {
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
                            studentHelper = new Student(image, fullName, email, institution, registrationNo);

                            reference.child(user.getUid()).setValue(studentHelper, (error, ref) -> System.err.println("Value was set. Error = "+error));

                            mPrefs.edit().putString(loggedStatus,"nouser").apply();
                            startActivity(new Intent(RegisterStudentActivity.this, MainActivity.class));
                        }).addOnFailureListener(e -> {
                            loading.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), "Verification Email not sent", Toast.LENGTH_SHORT).show();
                            AuthCredential credential = EmailAuthProvider.getCredential(email, password);

                            user.reauthenticate(credential)
                                    .addOnCompleteListener(task1 -> //Toast.makeText(RegisterStudentActivity.this, "Deleted User Successfully,", Toast.LENGTH_LONG).show();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
}