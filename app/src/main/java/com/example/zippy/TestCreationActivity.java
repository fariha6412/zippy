package com.example.zippy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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

import com.example.zippy.helper.InstructorHelperClass;
import com.example.zippy.helper.MenuHelperClass;
import com.example.zippy.helper.TestHelperClass;
import com.example.zippy.helper.ValidationChecker;
import com.example.zippy.utility.NetworkChangeListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class TestCreationActivity extends AppCompatActivity {
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    SharedPreferences mPrefs;
    private EditText editTXTcoursetitle, editTXTtotalmark, editTXTconvertto;
    private ProgressBar loading;
    FirebaseAuth auth;
    FirebaseDatabase rootNode;
    DatabaseReference reference;
    TestHelperClass testhelperclass;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_creation);

        //
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        auth = FirebaseAuth.getInstance();

        editTXTcoursetitle = findViewById(R.id.edittxtcoursetitle);
        editTXTtotalmark = findViewById(R.id.edittxttotalmark);
        editTXTconvertto = findViewById(R.id.edittxtconvertto);
        loading = findViewById(R.id.loading);
        // TextView txtViewLogin = findViewById(R.id.txtviewlogin);
        Button submitbtn = findViewById(R.id.submitbtn);

        Toolbar mtoolbar = findViewById(R.id.mtoolbar);
        setSupportActionBar(mtoolbar);

        submitbtn.setOnClickListener(v -> {
                    String courseTitle = editTXTcoursetitle.getText().toString().trim();
                    String totalMark = editTXTtotalmark.getText().toString().trim();
                    String convertTo = editTXTconvertto.getText().toString().trim();

                    if (ValidationChecker.isFieldEmpty(courseTitle, editTXTcoursetitle)) return;
                    if (!ValidationChecker.isValidEmail(totalMark, editTXTtotalmark)) return;
                    if (ValidationChecker.isFieldEmpty(convertTo, editTXTconvertto)) return;
                }
        );


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

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
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
}