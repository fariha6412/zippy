package com.example.zippy.ui.course;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.zippy.R;
import com.example.zippy.helper.EmailSender;
import com.example.zippy.helper.MenuHelper;
import com.example.zippy.classes.Student;
import com.example.zippy.ui.chat.ChatActivity;
import com.example.zippy.utility.NetworkChangeListener;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

public class AfterStudentDetailsActivity extends AppCompatActivity {
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    SharedPreferences mPrefs;
    final String strClickedUid = "clickedUid";
    private String clickedUid;
    final String strClickedCoursePassCode = "clickedCoursePassCode";
    String clickedCoursePassCode;

    private ImageView imgView;
    private TextView txtViewFullName, txtViewEmail, txtViewInstitution, txtViewRegistrationNo;
    private TextView txtViewAttendancePercentage, txtViewFinalGrade;
    private TextView txtViewYourMarkOnAttendance, txtViewYourTotalMark;

    private Boolean isFabHidden;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_student_details);
        Toolbar toolbar = findViewById(R.id.mToolbar);
        setSupportActionBar(toolbar);
        MenuHelper menuHelper = new MenuHelper(toolbar, this);
        menuHelper.handle();

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        clickedUid = mPrefs.getString(strClickedUid, "");
        clickedCoursePassCode = mPrefs.getString(strClickedCoursePassCode, "");
        imgView = findViewById(R.id.imgView);
        FloatingActionButton sendMail = findViewById(R.id.email_fab);
        FloatingActionButton sendMessage = findViewById(R.id.message_fab);
        ExtendedFloatingActionButton contact = findViewById(R.id.contact_fab);


        txtViewFullName = findViewById(R.id.txtViewFullName);
        txtViewEmail = findViewById(R.id.txtviewemail);
        txtViewRegistrationNo = findViewById(R.id.txtViewRegistrationNo);
        txtViewInstitution = findViewById(R.id.txtViewInstitution);

        txtViewAttendancePercentage = findViewById(R.id.txtviewattendancepercentage);
        txtViewFinalGrade = findViewById(R.id.txtViewFinalGrade);
        txtViewYourMarkOnAttendance = findViewById(R.id.txtViewResultedMarkOnAttendance);
        txtViewYourTotalMark = findViewById(R.id.txtViewTotalMark);
        txtViewFinalGrade = findViewById(R.id.txtViewFinalGrade);

        LinearLayout markOnAttendanceLinearLayout = findViewById(R.id.resultedMarkOnAttendanceLinearLayout);
        LinearLayout totalMarkLinearLayout = findViewById(R.id.totalMarkLinearLayout);
        LinearLayout finalGradeLinearLayout = findViewById(R.id.finalGradeLinearLayout);

        sendMail.setVisibility(View.GONE);
        sendMessage.setVisibility(View.GONE);

        isFabHidden = true;
        contact.shrink();

        String strIsCompleted = "isCompleted";
        boolean isCompleted = mPrefs.getBoolean(strIsCompleted, false);

        if(isCompleted){
            totalMarkLinearLayout.setVisibility(View.VISIBLE);
            finalGradeLinearLayout.setVisibility(View.VISIBLE);
            markOnAttendanceLinearLayout.setVisibility(View.VISIBLE);
            getResultData();
        }

        showStudentDetails();

        contact.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (isFabHidden) {
                            sendMail.show();
                            sendMessage.show();
                            contact.extend();
                            isFabHidden = false;
                        } else {
                            sendMail.hide();
                            sendMessage.hide();
                            contact.shrink();
                            isFabHidden = true;
                        }
                    }
                });
        sendMail.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EmailSender.sendEmailTo(AfterStudentDetailsActivity.this, new String[] {txtViewEmail.getText().toString().trim()});
                    }
                });
        sendMessage.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(AfterStudentDetailsActivity.this, ChatActivity.class));

                    }
                });
    }
    private void showStudentDetails(){
        DatabaseReference referenceStudent = FirebaseDatabase.getInstance().getReference("students/"+clickedUid);

        referenceStudent.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Student studentHelper = snapshot.getValue(Student.class);
                    assert studentHelper != null;
                    txtViewFullName.setText(studentHelper.getFullName());
                    txtViewInstitution.setText(studentHelper.getInstitution());
                    txtViewEmail.setText(studentHelper.getEmail());

                    txtViewRegistrationNo.setText(studentHelper.getRegistrationNo());
                    Glide.with(getBaseContext()).load(studentHelper.getImage()).into(imgView);
                    extractData();
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
    private void extractData(){
        DatabaseReference referenceAttendance = FirebaseDatabase.getInstance().getReference("attendanceRecord/total/" + clickedCoursePassCode + "/" + clickedUid);
        referenceAttendance.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    txtViewAttendancePercentage.setText(String.valueOf ((Long.parseLong(String.valueOf(snapshot.child("totalPresent").getValue())) *100.0 )/ (Long.parseLong(String.valueOf(snapshot.child("totalPresent").getValue())) + Long.parseLong(String.valueOf(snapshot.child("totalAbsent").getValue())))));
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
    private void getResultData(){
        DatabaseReference referenceResultAttendance = FirebaseDatabase.getInstance().getReference("result/"+clickedUid+"/"+clickedCoursePassCode+"/"+"attendance/got");
        DatabaseReference referenceResultFinalEvaluation = FirebaseDatabase.getInstance().getReference("result/"+clickedUid+"/"+clickedCoursePassCode+"/"+"finalEvaluation");
        referenceResultAttendance.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snpt) {
                if(snpt.exists()){
                    txtViewYourMarkOnAttendance.setText((String.valueOf(snpt.getValue())));
                }
                else txtViewYourMarkOnAttendance.setText("0");
                referenceResultFinalEvaluation.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot dst) {
                        txtViewYourTotalMark.setText(((String) dst.child("got").getValue()));
                        txtViewFinalGrade.setText((String) dst.child("finalGrade").getValue());
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
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