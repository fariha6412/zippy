package com.example.zippy.ui.course;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.zippy.R;
import com.example.zippy.helper.FileHelper;
import com.example.zippy.helper.MenuHelperClass;
import com.example.zippy.helper.ResultHelper;
import com.example.zippy.helper.TestHelperClass;
import com.example.zippy.ui.attendance.AttendanceTakingActivity;
import com.example.zippy.ui.test.TestCreationActivity;
import com.example.zippy.ui.test.TestDetailsActivity;
import com.example.zippy.utility.NetworkChangeListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

@RequiresApi(api = Build.VERSION_CODES.O)
public class CourseDetailsActivity extends AppCompatActivity {
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    private static final int CSV_PICK_CODE = 1010;

    private FirebaseDatabase rootNode;
    private DatabaseReference referenceCourse;

    private SharedPreferences mPrefs;
    final String strClickedCoursePassCode = "clickedCoursePassCode";
    String clickedCoursePassCode;
    final String strClickedTestId = "clickedTestId";
    final String strAlertGradingFormat = "alertGradingFormat";
    String clickedTestId;
    private final boolean[] flag = new boolean[1];

    private LinearLayout markOnAttendanceLinearLayout;
    private TextView txtViewMarkOnAttendance;
    private Button attendanceMarkAssignBtn;
    private Uri gradingScaleUri;
    private ArrayList<String> testIds;
    private Boolean isAssignedAttendanceMark = false;
    private Double markOnAttendance = null;
    private LocalDate dateToday;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_details);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        clickedCoursePassCode = mPrefs.getString(strClickedCoursePassCode, "");
        clickedTestId = mPrefs.getString(strClickedTestId, "");
        String strIsCompleted = "isCompleted";
        boolean isCompleted = mPrefs.getBoolean(strIsCompleted, false);
        boolean alertGradingFormat = mPrefs.getBoolean(strAlertGradingFormat,false);
        flag[0] = alertGradingFormat;

        Toolbar toolbar = findViewById(R.id.mToolbar);
        setSupportActionBar(toolbar);
        MenuHelperClass menuHelperClass = new MenuHelperClass(toolbar, this);
        menuHelperClass.handle();

        dateToday = LocalDate.now();
        rootNode = FirebaseDatabase.getInstance();

        TextView txtViewCoursePassCode = findViewById(R.id.txtviewcoursepasscode);
        txtViewMarkOnAttendance = findViewById(R.id.txtviewattendancemark);
        Button studentDetailsBtn = findViewById(R.id.studentdetailsbtn);
        Button attendanceBtn = findViewById(R.id.attendance);
        attendanceMarkAssignBtn = findViewById(R.id.attendancemark);
        Button completeBtn = findViewById(R.id.completeBtn);
        MaterialButton testCreationBtn = findViewById(R.id.testcreationbtn);
        MaterialButton editMarkOnAttendance = findViewById(R.id.markonattendancebtn);
        AutoCompleteTextView autoCompleteTextView = findViewById(R.id.autoCompleteTextView);
        markOnAttendanceLinearLayout = findViewById(R.id.markonattendancelinearlayout);

        testIds = new ArrayList<>();
        ArrayList<String> testTitles = new ArrayList<>();

        TestHelperClass.getTestList(this, testIds, testTitles, autoCompleteTextView, clickedCoursePassCode);
        autoCompleteTextView.setThreshold(0);
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                System.out.println(testIds.get(i));
                mPrefs.edit().putString(strClickedTestId,testIds.get(i)).apply();
                startActivity(new Intent(CourseDetailsActivity.this, TestDetailsActivity.class));
            }
        });

        txtViewCoursePassCode.setText(clickedCoursePassCode);

        referenceCourse = rootNode.getReference("courses/" + clickedCoursePassCode);
        getAttendanceStatus();

        testCreationBtn.setOnClickListener(v -> {
            if(isCompleted){
                Toast.makeText(this, "Course is completed", Toast.LENGTH_SHORT).show();
            }
            else startActivity(new Intent(CourseDetailsActivity.this, TestCreationActivity.class));
        });

        studentDetailsBtn.setOnClickListener(v -> {
            startActivity(new Intent(CourseDetailsActivity.this, StudentDetailsActivity.class));
        });
        attendanceBtn.setOnClickListener(v -> {
            if(isCompleted){
                Toast.makeText(this, "Course is completed", Toast.LENGTH_SHORT).show();
            }
            else {
                DatabaseReference referenceAttendance = rootNode.getReference("attendanceRecord/perDay/" + clickedCoursePassCode + "/" + dateToday);
                referenceAttendance.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Toast.makeText(getApplicationContext(), "Attendance already taken for today.", Toast.LENGTH_SHORT).show();
                        } else {
                            startActivity(new Intent(CourseDetailsActivity.this, AttendanceTakingActivity.class));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
            }
        });
        editMarkOnAttendance.setOnClickListener(v -> {
            if(isCompleted){
                Toast.makeText(this, "Course is completed", Toast.LENGTH_SHORT).show();
            }
            else assignAttendanceMark();
        });
        attendanceMarkAssignBtn.setOnClickListener(v -> {
            if(isCompleted){
                Toast.makeText(this, "Course is completed", Toast.LENGTH_SHORT).show();
            }
            else assignAttendanceMark();
        });


        completeBtn.setOnClickListener(v -> {
            if(isCompleted){
                Toast.makeText(this, "Course is completed", Toast.LENGTH_SHORT).show();
            }
            else {
                if(!flag[0]){

                    new AlertDialog.Builder(CourseDetailsActivity.this)
                            .setTitle("Message")
                            .setMessage("Do you want a sample grading scale [as this is your first time and you won't be seeing this again]?")
                            .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    FileHelper.downloadFile(CourseDetailsActivity.this, "samples/sampleGradingScale.csv", "sampleGradingScale", ".csv");
                                }
                            })
                            .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String warningDetails = "Please upload a csv file with two columns [upper limit of mark, grade]. You won't be able to re-upload if you make any mistake.";
                                    FileHelper.alertForCsvFormat(CourseDetailsActivity.this, warningDetails);
                                }
                            }).create().show();
                    flag[0] = true;
                    mPrefs.edit().putBoolean(strAlertGradingFormat,true).apply();
                }
                else {
                    FileHelper.filePickIntent(CourseDetailsActivity.this, CSV_PICK_CODE);
                }
            }
        });
    }
    @SuppressLint("SetTextI18n")
    private void uploadGradingScale(){
        ProgressDialog progressDialog = new ProgressDialog(CourseDetailsActivity.this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setMessage("Uploading CSV");
        progressDialog.show();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String gradingPath = "gradingScales/" +user.getUid()+"/"+ clickedCoursePassCode + "/gradingScale";
        StorageReference storageReferenceCsv = FirebaseStorage.getInstance().getReference(gradingPath);
        storageReferenceCsv.putFile(gradingScaleUri).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
            while (!uriTask.isSuccessful());
            String uploadedGradingScaleUrl = ""+uriTask.getResult();

            progressDialog.dismiss();
            Toast.makeText(CourseDetailsActivity.this, "Uploaded successfully", Toast.LENGTH_SHORT).show();

            referenceCourse.child("gradingScaleUrl").setValue(uploadedGradingScaleUrl);
            courseComplete();
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(CourseDetailsActivity.this, "pdf upload failed due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        if(resultCode == RESULT_OK) {
            if(requestCode == CSV_PICK_CODE){
                gradingScaleUri = FileHelper.findPickedFileUri(this, null, requestCode, data );
                if(gradingScaleUri != null){
                    uploadGradingScale();
                }
            }
        }
        else {
            Toast.makeText(this, "Selection cancelled", Toast.LENGTH_SHORT).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    private void assignAttendanceMark(){
            EditText mark = new EditText(CourseDetailsActivity.this);
            AlertDialog.Builder gettingMarkDialog = new AlertDialog.Builder(CourseDetailsActivity.this);
            gettingMarkDialog.setTitle("Mark On Attendance").setCancelable(true) ;
            gettingMarkDialog.setView(mark);
            DialogInterface.OnClickListener yesBtnFunc = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    boolean flag = false;
                }

            };
            gettingMarkDialog.setPositiveButton("SET", yesBtnFunc);
            gettingMarkDialog.setNegativeButton("CANCEL", null);
            AlertDialog alert = gettingMarkDialog.create();
            alert.show();

            alert.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    try {
                        double markDbl = Double.parseDouble(mark.getText().toString().trim());
                        if(markDbl < 0 || markDbl > 100){
                            mark.setError("Give a valid mark");
                            mark.requestFocus();
                        }
                        else{
                            referenceCourse.child("markOnAttendance").setValue(markDbl, (error, ref) -> isAssignedAttendanceMark = true);
                            txtViewMarkOnAttendance.setText(String.valueOf(markDbl));
                            markOnAttendanceLinearLayout.setVisibility(View.VISIBLE);
                            attendanceMarkAssignBtn.setVisibility(View.GONE);
                            isAssignedAttendanceMark = true;
                            alert.dismiss();
                        }
                    }
                    catch (Exception e){
                        mark.setError("Give a Valid number");
                        mark.requestFocus();
                    }
                }
            });
    }
    private void getAttendanceStatus(){
        referenceCourse.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Object markOnAttendanceOBJ = snapshot.child("markOnAttendance").getValue();

                isAssignedAttendanceMark = markOnAttendanceOBJ != null;

                if(isAssignedAttendanceMark){
                    try{
                        markOnAttendance = (Double) markOnAttendanceOBJ;
                    }catch (ClassCastException e) {
                        markOnAttendance = ((Long) markOnAttendanceOBJ)*1.0;
                    }
                    txtViewMarkOnAttendance.setText(markOnAttendance.toString());
                    markOnAttendanceLinearLayout.setVisibility(View.VISIBLE);
                    attendanceMarkAssignBtn.setVisibility(View.GONE);
                }
                markOnAttendance = 0.0;
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
    private void courseComplete() {
        HashMap<Integer, String> gradingScaleHash = FileHelper.readGradingScaleCsv(gradingScaleUri, CourseDetailsActivity.this);
        if(gradingScaleHash == null) {
            Toast.makeText(CourseDetailsActivity.this, "its null", Toast.LENGTH_SHORT).show();
            return;
        }
        ProgressDialog progressDialog;
        progressDialog = new ProgressDialog(CourseDetailsActivity.this);
        progressDialog.setTitle("wait");
        progressDialog.setMessage("Making final evaluation");
        progressDialog.show();
        ResultHelper.calculateAndStoreFinalResult(CourseDetailsActivity.this, clickedCoursePassCode, gradingScaleHash, markOnAttendance);
        progressDialog.dismiss();
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
    //end stuff
}