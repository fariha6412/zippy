package com.example.zippy.ui.test;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.zippy.R;
import com.example.zippy.helper.FileHelper;
import com.example.zippy.helper.MenuHelperClass;
import com.example.zippy.helper.ResultHelper;
import com.example.zippy.helper.TestHelperClass;
import com.example.zippy.ui.course.CourseDetailsActivity;
import com.example.zippy.utility.NetworkChangeListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class TestDetailsActivity extends AppCompatActivity {
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    private static final int PDF_PICK_CODE = 1000;
    private static final int CSV_PICK_CODE = 1010;

    private DatabaseReference referenceResult, referenceTest;

    final String loggedStatus = "loggedProfile";
    private String loggedProfile;
    final String strClickedCoursePassCode = "clickedCoursePassCode";
    private String clickedCoursePassCode;
    final String strClickedTestId = "clickedTestId";
    private String clickedTestId;
    final String strAlertCsvFormat = "alertCsvFormat";

    private Uri pdfUri;
    private Uri markSheetUri;
    private String totalMark ="0";
    private String convertTo ="0";
    private String pdfFilePath;
    private String markSheetPath;
    private String instructorUID;
    private Boolean hasQuestion;
    private Boolean hasMarkSheet;

    private TextView txtViewUploadQuestionFileName, txtViewUploadMarkSheetFileName;
    private TextView txtViewTestTitle, txtViewTotalMarks, txtViewConvertTo, txtViewYourTotalMarks, txtViewAfterConversion;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_details);

        Toolbar toolbar = findViewById(R.id.mToolbar);
        setSupportActionBar(toolbar);
        MenuHelperClass menuHelperClass = new MenuHelperClass(toolbar, this);
        menuHelperClass.handle();

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        loggedProfile = mPrefs.getString(loggedStatus, "nouser");
        clickedCoursePassCode = mPrefs.getString(strClickedCoursePassCode, "");
        clickedTestId = mPrefs.getString(strClickedTestId, "");
        String strIsCompleted = "isCompleted";
        boolean isCompleted = mPrefs.getBoolean(strIsCompleted, false);
        boolean alertCsvFormat = mPrefs.getBoolean(strAlertCsvFormat, false);
        final boolean[] flag = {alertCsvFormat};

        referenceTest = FirebaseDatabase.getInstance().getReference("tests/"+clickedCoursePassCode+"/"+clickedTestId);

        txtViewTotalMarks = findViewById(R.id.txtviewtotalmark);
        txtViewConvertTo = findViewById(R.id.txtviewconvertto);
        txtViewYourTotalMarks = findViewById(R.id.txtviewyourtotalmark);
        txtViewAfterConversion = findViewById(R.id.txtviewafterconvertion);

        txtViewTestTitle = findViewById(R.id.txtviewtesttitle);

        txtViewUploadQuestionFileName = findViewById(R.id.uploadquestionfilename);
        txtViewUploadMarkSheetFileName = findViewById(R.id.uploadmarksheetfilename);

        LinearLayout yourTotalMarkLinearLayout = findViewById(R.id.yourTotalMarkLinearLayout);
        LinearLayout afterConversionLinearLayout = findViewById(R.id.afterConversionLinearLayout);
        LinearLayout markSheetLinearLayout = findViewById(R.id.markSheetLinearLayout);

        MaterialButton editTotalMarkBtn = findViewById(R.id.chgtotalmarksbtn);
        MaterialButton editConvertToBtn = findViewById(R.id.chgconverttobtn);
        MaterialButton uploadQuestionBtn = findViewById(R.id.uploadquestionbtn);
        MaterialButton uploadMarkSheetBtn = findViewById(R.id.uploadmarksheetbtn);
        MaterialButton downloadQuestionBtn = findViewById(R.id.downloadquestionbtn);
        MaterialButton downloadMarkSheetBtn = findViewById(R.id.downloadmarksheetbtn);

        if(loggedProfile.equals("instructor")){
            if(isCompleted){
                editTotalMarkBtn.setVisibility(View.GONE);
                editConvertToBtn.setVisibility(View.GONE);
                uploadQuestionBtn.setVisibility(View.GONE);
                uploadMarkSheetBtn.setVisibility(View.GONE);
            }
            yourTotalMarkLinearLayout.setVisibility(View.GONE);
            afterConversionLinearLayout.setVisibility(View.GONE);
        }
        if(loggedProfile.equals("student")){
            markSheetLinearLayout.setVisibility(View.GONE);
            editConvertToBtn.setVisibility(View.GONE);
            editTotalMarkBtn.setVisibility(View.GONE);
            uploadQuestionBtn.setVisibility(View.GONE);
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            assert user != null;
            referenceResult = FirebaseDatabase.getInstance().getReference("result/"+ user.getUid()+"/"+clickedCoursePassCode+"/tests/"+clickedTestId);
        }

        txtViewTotalMarks.setText(totalMark);
        txtViewConvertTo.setText(convertTo);
        String yourTotalMark = "0";
        txtViewYourTotalMarks.setText(yourTotalMark);
        String afterConversion = "0";
        txtViewAfterConversion.setText(afterConversion);

        getTestDetails();
        getQuestionAndMarkSheetStatus();
        if(loggedProfile.equals("student"))getResult();

        editTotalMarkBtn.setOnClickListener(v -> {
            changeTotalMark();
        });
        editConvertToBtn.setOnClickListener(v -> {
            changeConvertTo();
        });
        uploadQuestionBtn.setOnClickListener(v -> {
            FileHelper.filePickIntent(this, PDF_PICK_CODE);
        });
        uploadMarkSheetBtn.setOnClickListener(v -> {
            if(!flag[0]){
                String warningDetails = "Please upload a csv file with three columns [registrationNo, totalMark, convertedMark]";
                FileHelper.alertForCsvFormat(TestDetailsActivity.this, warningDetails);
                flag[0] = true;
                mPrefs.edit().putBoolean(strAlertCsvFormat,true).apply();
            }
            else checkHasQuestion();
        });
        downloadQuestionBtn.setOnClickListener(v -> {
            FileHelper.downloadFile(this, pdfFilePath, "question", ".pdf");
        });
        downloadMarkSheetBtn.setOnClickListener(v -> {
            FileHelper.downloadFile(this, markSheetPath, "markSheet", ".csv");
        });
    }

    public void checkHasQuestion(){
        referenceTest.child("questionPdfUrl").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String questionUrl = (String) snapshot.getValue();
                    assert questionUrl != null;
                    if(questionUrl.equals("")){
                        new AlertDialog.Builder(TestDetailsActivity.this)
                                .setTitle("Message")
                                .setMessage("You haven't uploaded any question")
                                .setPositiveButton("OKAY", null).create().show();
                    }
                    else {
                        FileHelper.filePickIntent(TestDetailsActivity.this, CSV_PICK_CODE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        if(resultCode == RESULT_OK) {
            if(requestCode == PDF_PICK_CODE){
                pdfUri = FileHelper.findPickedFileUri(this, txtViewUploadQuestionFileName, requestCode, data );
                if(pdfUri != null)uploadPdfToDatabase();
            }
            else {
                markSheetUri = FileHelper.findPickedFileUri(this, txtViewUploadMarkSheetFileName, requestCode, data );
                if(markSheetUri != null){
                    ArrayList<TestHelperClass.TestMark> testMarks;
                    testMarks = FileHelper.readMarkSheetCsv(markSheetUri, TestDetailsActivity.this);
                    if(testMarks != null) uploadMarkSheetToDatabase(testMarks);
                }
            }
        }
        else {
            Toast.makeText(this, "Selection cancelled", Toast.LENGTH_SHORT).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    private void getTestDetails(){
        referenceTest.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    TestHelperClass testHelper = snapshot.getValue(TestHelperClass.class);
                    assert testHelper != null;
                    totalMark = testHelper.getTotalMark().toString();
                    convertTo = testHelper.getConvertTo().toString();
                    txtViewTestTitle.setText(testHelper.getTestTitle());
                    txtViewTotalMarks.setText(totalMark);
                    txtViewConvertTo.setText(convertTo);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
    private void getResult(){
        referenceResult.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    txtViewYourTotalMarks.setText(String.valueOf(snapshot.child("totalMark").getValue()));
                    txtViewAfterConversion.setText(String.valueOf(snapshot.child("convertedMark").getValue()));
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
    private void uploadPdfToDatabase(){
        ProgressDialog progressDialog = new ProgressDialog(TestDetailsActivity.this);
        progressDialog.setTitle("Wait");
        progressDialog.setMessage("Uploading PDF");
        progressDialog.show();
        StorageReference storageReferencePdf = FirebaseStorage.getInstance().getReference(pdfFilePath);
        storageReferencePdf.putFile(pdfUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                String uploadedPdfUrl = ""+uriTask.getResult();
                progressDialog.dismiss();
                referenceTest.child("questionPdfUrl").setValue(uploadedPdfUrl);
                Toast.makeText(TestDetailsActivity.this, "Uploaded successfully", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(TestDetailsActivity.this, "pdf upload failed due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
    private void uploadMarkSheetToDatabase(ArrayList<TestHelperClass.TestMark> testMarks){
        ProgressDialog progressDialog = new ProgressDialog(TestDetailsActivity.this);
        progressDialog.setTitle("Wait");
        progressDialog.setMessage("Uploading CSV");
        progressDialog.show();
        StorageReference storageReferenceCsv = FirebaseStorage.getInstance().getReference(markSheetPath);
        storageReferenceCsv.putFile(markSheetUri).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
            while (!uriTask.isSuccessful());
            String uploadedMarkSheetUrl = ""+uriTask.getResult();

            progressDialog.dismiss();
            Toast.makeText(TestDetailsActivity.this, "Uploaded successfully", Toast.LENGTH_SHORT).show();

            ResultHelper.writeTestResultToDatabase(TestDetailsActivity.this, clickedCoursePassCode, testMarks,clickedTestId);

            referenceTest.child("markSheetCsvUrl").setValue(uploadedMarkSheetUrl);
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(TestDetailsActivity.this, "pdf upload failed due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
    private void getQuestionAndMarkSheetStatus(){
        DatabaseReference referenceCourse = FirebaseDatabase.getInstance().getReference("courses/"+clickedCoursePassCode);
        referenceCourse.child("instructoruid").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                instructorUID = (String) snapshot.getValue();

                pdfFilePath = "questionPdfs/"+instructorUID+"/"+clickedCoursePassCode+"/"+clickedTestId+"/question";
                markSheetPath = "markSheets/"+instructorUID+"/"+clickedCoursePassCode+"/"+clickedTestId+"/markSheet";
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
        referenceTest.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String question = (String) snapshot.child("questionPdfUrl").getValue();
                String markSheet = (String) snapshot.child("markSheetCsvUrl").getValue();

                assert question != null;
                hasQuestion = !question.equals("");
                assert markSheet != null;
                hasMarkSheet = !markSheet.equals("");

                System.out.println(hasQuestion);
                if(hasQuestion){
                    txtViewUploadQuestionFileName.setText("uploaded");
                    txtViewUploadQuestionFileName.setVisibility(View.VISIBLE);
                }else{
                    txtViewUploadQuestionFileName.setVisibility(View.GONE);
                }
                if(hasMarkSheet && loggedProfile.equals("instructor")){
                    txtViewUploadMarkSheetFileName.setText("uploaded");
                    txtViewUploadMarkSheetFileName.setVisibility(View.VISIBLE);
                }else{
                    txtViewUploadMarkSheetFileName.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
    private void changeTotalMark(){
        EditText mark = new EditText(TestDetailsActivity.this);
        AlertDialog.Builder gettingMarkDialog = new AlertDialog.Builder(TestDetailsActivity.this);
        gettingMarkDialog.setTitle("Change total mark").setCancelable(true) ;
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
                    Long markLng = Long.parseLong(mark.getText().toString().trim());
                    if(markLng < 0 || markLng > 100){
                        mark.setError("Give a valid mark");
                        mark.requestFocus();
                    }
                    else{
                        referenceTest.child("totalMark").setValue(markLng);
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
    private void changeConvertTo(){
        EditText mark = new EditText(TestDetailsActivity.this);
        AlertDialog.Builder gettingMarkDialog = new AlertDialog.Builder(TestDetailsActivity.this);
        gettingMarkDialog.setTitle("Change converting mark").setCancelable(true) ;
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
                        referenceTest.child("convertTo").setValue(markDbl);
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