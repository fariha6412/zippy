package com.example.zippy.ui.test;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.zippy.R;
import com.example.zippy.classes.Test;
import com.example.zippy.helper.FileHelper;
import com.example.zippy.helper.MenuHelper;
import com.example.zippy.helper.PassCodeGenerator;
import com.example.zippy.helper.ResultHelper;
import com.example.zippy.helper.TestHelper;
import com.example.zippy.helper.ValidationChecker;
import com.example.zippy.utility.NetworkChangeListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

public class TestCreationActivity extends AppCompatActivity {
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    private static final int PDF_PICK_CODE = 1000;
    private static final int CSV_PICK_CODE = 1010;

    final String strClickedCoursePassCode = "clickedCoursePassCode";
    String clickedCoursePassCode;
    final String strAlertCsvFormat = "alertCsvFormat";
    Boolean alertCsvFormat;

    private DatabaseReference referenceTests;

    private EditText editTXTTestTitle, editTXTTotalMarks, editTXTConvertTo;
    private TextView txtViewUploadQuestionFileName, txtViewUploadMarkSheetFileName;
    private Uri pdfUri;
    private Uri markSheetUri;
    private String pdfFilePath;
    private String markSheetPath;
    private String instructorUID;
    private String testId;

    private ArrayList<String> testIds;
    private ArrayList<TestHelper.TestMark> testMarks;

    private Test test;

    private ProgressDialog progressDialog;

    private Boolean flag;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_creation);

        Toolbar toolbar = findViewById(R.id.mToolbar);
        setSupportActionBar(toolbar);
        MenuHelper menuHelper = new MenuHelper(toolbar, this);
        menuHelper.handle();

        editTXTTestTitle = findViewById(R.id.edittxttesttitle);
        editTXTTotalMarks = findViewById(R.id.edittxttotalmark);
        editTXTConvertTo = findViewById(R.id.edittxtconvertto);

        txtViewUploadQuestionFileName = findViewById(R.id.uploadquestionfilename);
        txtViewUploadMarkSheetFileName = findViewById(R.id.uploadmarksheetfilename);

        Button questionUploadBtn = findViewById(R.id.uploadquestionbtn);
        Button markSheetUploadBtn = findViewById(R.id.uploadmarksheetbtn);
        Button submitBtn = findViewById(R.id.submitbtn);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCancelable(false);

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        clickedCoursePassCode = mPrefs.getString(strClickedCoursePassCode, "");
        alertCsvFormat = mPrefs.getBoolean(strAlertCsvFormat,false);
        flag = alertCsvFormat;

        testIds = new ArrayList<>();
        testMarks = new ArrayList<>();

        FirebaseDatabase rootNode = FirebaseDatabase.getInstance();
        referenceTests = rootNode.getReference("tests/"+clickedCoursePassCode);

        questionUploadBtn.setOnClickListener(v -> FileHelper.filePickIntent(this, PDF_PICK_CODE));
        markSheetUploadBtn.setOnClickListener(v -> {
            if(pdfUri == null || txtViewUploadQuestionFileName.getVisibility() == View.GONE){
                new AlertDialog.Builder(TestCreationActivity.this)
                        .setTitle("Message")
                        .setMessage("You haven't uploaded any question")
                        .setPositiveButton("OKAY", null).create().show();
            }
            else {
                if(!flag){
                    String warningDetails = "Please upload a csv file with three comma separated values each line [registrationNo, totalMark, convertedMark]";
                    FileHelper.alertForCsvFormat(TestCreationActivity.this, warningDetails);
                    flag = true;
                    mPrefs.edit().putBoolean(strAlertCsvFormat,true).apply();
                }
                else FileHelper.filePickIntent(this, CSV_PICK_CODE);
            }
        });
        submitBtn.setOnClickListener(v -> writeToDataBase());
        txtViewUploadQuestionFileName.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;

            if(event.getAction() == MotionEvent.ACTION_UP) {
                if(event.getRawX() >= (txtViewUploadQuestionFileName.getRight() - txtViewUploadQuestionFileName.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    txtViewUploadQuestionFileName.setVisibility(View.GONE);
                    txtViewUploadMarkSheetFileName.setVisibility(View.GONE);
                    return true;
                }
            }
            return false;
        });
        txtViewUploadMarkSheetFileName.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;

            if(event.getAction() == MotionEvent.ACTION_UP) {
                if(event.getRawX() >= (txtViewUploadMarkSheetFileName.getRight() - txtViewUploadMarkSheetFileName.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    txtViewUploadMarkSheetFileName.setVisibility(View.GONE);
                    return true;
                }
            }
            return false;
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        if(resultCode == RESULT_OK) {
            if(requestCode == PDF_PICK_CODE)pdfUri = FileHelper.findPickedFileUri(this, txtViewUploadQuestionFileName, requestCode, data );
            else markSheetUri = FileHelper.findPickedFileUri(this, txtViewUploadMarkSheetFileName, requestCode, data );
        }
        else {
            Toast.makeText(this, "Selection cancelled", Toast.LENGTH_SHORT).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void writeToDataBase() {
        String testTitle = editTXTTestTitle.getText().toString().trim();
        String totalMarks = editTXTTotalMarks.getText().toString().trim();
        String convertTo = editTXTConvertTo.getText().toString().trim();
        long totalMarksL;
        double convertToDbl;

        if (ValidationChecker.isFieldEmpty(testTitle, editTXTTestTitle)) return;
        if (ValidationChecker.isFieldEmpty(totalMarks, editTXTTotalMarks)) return;
        if (ValidationChecker.isFieldEmpty(convertTo, editTXTConvertTo)) return;
        try {
            totalMarksL = Long.parseLong(totalMarks);
        } catch (Exception e) {
            editTXTTotalMarks.setError("Enter a number");
            editTXTTotalMarks.requestFocus();
            return;
        }
        try {
            convertToDbl = Double.parseDouble(convertTo);
        } catch (Exception e) {
            editTXTConvertTo.setError("Enter a number");
            editTXTConvertTo.requestFocus();
            return;
        }
        test = new Test(testTitle, totalMarksL, convertToDbl);

        referenceTests.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for(DataSnapshot dsnap:snapshot.getChildren()){
                    String testId = dsnap.getKey();
                    testIds.add(testId);
                }
                testId = createNewCode();
                while(testIds.contains(testId))testId = createNewCode();
                getQuestionAndMarkSheetPath(test);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
    private void uploadFilesToStorage(Test test){
        progressDialog.setMessage("Uploading Files");
        if(txtViewUploadMarkSheetFileName.getVisibility() == View.GONE && txtViewUploadQuestionFileName.getVisibility() == View.VISIBLE){
            progressDialog.show();
            StorageReference storageReferencePdf = FirebaseStorage.getInstance().getReference(pdfFilePath);
            storageReferencePdf.putFile(pdfUri).addOnSuccessListener(taskSnapshot -> {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                String uploadedPdfUrl = ""+uriTask.getResult();
                progressDialog.dismiss();
                test.setQuestionPdfUrl(uploadedPdfUrl);
                TestHelper.createTest(TestCreationActivity.this, referenceTests, test, testId);
                finish();
            }).addOnFailureListener(e -> Toast.makeText(TestCreationActivity.this, "pdf upload failed due to "+e.getMessage(), Toast.LENGTH_SHORT).show());
        }
        else if(txtViewUploadQuestionFileName.getVisibility() == View.GONE){
            TestHelper.createTest(this, referenceTests, test, testId);
            finish();
        }
        else{
            //markSheet and pdf both
            testMarks = FileHelper.readMarkSheetCsv(markSheetUri, TestCreationActivity.this);
            if(testMarks == null){
                txtViewUploadMarkSheetFileName.setVisibility(View.GONE);
                return;
            }
            progressDialog.show();
            StorageReference storageReferencePdf = FirebaseStorage.getInstance().getReference(pdfFilePath);
            storageReferencePdf.putFile(pdfUri).addOnSuccessListener(taskSnapshot -> {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                String uploadedPdfUrl = ""+uriTask.getResult();
                test.setQuestionPdfUrl(uploadedPdfUrl);

                StorageReference storageReferenceCsv = FirebaseStorage.getInstance().getReference(markSheetPath);
                storageReferenceCsv.putFile(markSheetUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String uploadedMarkSheetUrl = ""+uriTask.getResult();

                        test.setMarkSheetCsvUrl(uploadedMarkSheetUrl);
                        progressDialog.dismiss();
                        ResultHelper.writeTestResultToDatabase(TestCreationActivity.this, clickedCoursePassCode, testMarks,testId);
                        TestHelper.createTest(TestCreationActivity.this, referenceTests, test, testId);
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(TestCreationActivity.this, "csv upload failed due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull @NotNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(TestCreationActivity.this, "pdf upload failed due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    private void getQuestionAndMarkSheetPath(Test testHelper) {
        DatabaseReference referenceCourse = FirebaseDatabase.getInstance().getReference("courses/" + clickedCoursePassCode);
        referenceCourse.child("instructorUID").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                instructorUID = (String) snapshot.getValue();

                pdfFilePath = "questionPdfs/" + instructorUID + "/" + clickedCoursePassCode + "/" + testId + "/question";
                markSheetPath = "markSheets/" + instructorUID + "/" + clickedCoursePassCode + "/" + testId + "/markSheet";

                uploadFilesToStorage(testHelper);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
    public String createNewCode(){
        PassCodeGenerator passCodeGenerator = new PassCodeGenerator.PassCodeGeneratorBuilder()
                .useDigits(true)
                .useLower(true)
                .useUpper(true)
                .usePunctuation(true)
                .build();
        return passCodeGenerator.generate(4);
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.profile_menu, menu);
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