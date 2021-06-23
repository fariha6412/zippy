package com.example.zippy.ui.test;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.OpenableColumns;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zippy.R;
import com.example.zippy.helper.FileHelper;
import com.example.zippy.helper.MenuHelperClass;
import com.example.zippy.helper.PassCodeGenerator;
import com.example.zippy.helper.TestHelperClass;
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

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class TestCreationActivity extends AppCompatActivity {
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    private static final int PDF_PICK_CODE = 1000;
    private static final int CSV_PICK_CODE = 1010;

    SharedPreferences mPrefs;
    final String strClickedCoursePassCode = "clickedCoursePassCode";
    String clickedCoursePassCode;

    FirebaseDatabase rootNode;
    DatabaseReference referenceTests, referenceResult, referenceStudentList, referenceStudentRegNo;

    EditText editTXTTestTitle, editTXTTotalMarks, editTXTConvertTo;
    TextView txtViewUploadQuestionFileName, txtViewUploadMarkSheetFileName;
    Button questionUploadBtn, markSheetUploadBtn, submitBtn;
    private Uri pdfUri;
    private Uri markSheetUri;

    ArrayList<String> testIds;
    ArrayList<TestHelperClass.TestMark> testMarks;

    TestHelperClass testHelper;

    ProgressDialog progressDialog;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_creation);

        Toolbar mtoolbar = findViewById(R.id.mtoolbar);
        setSupportActionBar(mtoolbar);
        MenuHelperClass menuHelperClass = new MenuHelperClass(mtoolbar, this);
        menuHelperClass.handle();

        editTXTTestTitle = findViewById(R.id.edittxttesttitle);
        editTXTTotalMarks = findViewById(R.id.edittxttotalmark);
        editTXTConvertTo = findViewById(R.id.edittxtconvertto);

        txtViewUploadQuestionFileName = findViewById(R.id.uploadquestionfilename);
        txtViewUploadMarkSheetFileName = findViewById(R.id.uploadmarksheetfilename);

        questionUploadBtn = findViewById(R.id.uploadquestionbtn);
        markSheetUploadBtn = findViewById(R.id.uploadmarksheetbtn);
        submitBtn = findViewById(R.id.submitbtn);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCancelable(false);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        clickedCoursePassCode = mPrefs.getString(strClickedCoursePassCode, "");

        testIds = new ArrayList<>();
        testMarks = new ArrayList<>();

        rootNode = FirebaseDatabase.getInstance();
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
                FileHelper.filePickIntent(this, CSV_PICK_CODE);
            }
        });
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeToDataBase();
            }
        });
        txtViewUploadQuestionFileName.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_RIGHT = 2;

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() >= (txtViewUploadQuestionFileName.getRight() - txtViewUploadQuestionFileName.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        txtViewUploadQuestionFileName.setVisibility(View.GONE);
                        txtViewUploadMarkSheetFileName.setVisibility(View.GONE);
                        return true;
                    }
                }
                return false;
            }
        });
        txtViewUploadMarkSheetFileName.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_RIGHT = 2;

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() >= (txtViewUploadMarkSheetFileName.getRight() - txtViewUploadMarkSheetFileName.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        txtViewUploadMarkSheetFileName.setVisibility(View.GONE);
                        return true;
                    }
                }
                return false;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        if(resultCode == RESULT_OK) {
            if(requestCode == PDF_PICK_CODE)pdfUri = FileHelper.onActivityResult(this, txtViewUploadQuestionFileName, requestCode, data );
            else markSheetUri = FileHelper.onActivityResult(this, txtViewUploadMarkSheetFileName, requestCode, data );
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
        Long totalMarksL;
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
        testHelper = new TestHelperClass(testTitle, totalMarksL, convertToDbl);

        referenceTests.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for(DataSnapshot dsnap:snapshot.getChildren()){
                    String testId = dsnap.getKey();
                    testIds.add(testId);
                }
                String newTestId = createNewCode();
                while(testIds.contains(newTestId))newTestId = createNewCode();
                uploadFilesToStorage(testHelper, newTestId);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
    private void uploadFilesToStorage(TestHelperClass testHelper, String testId){
        progressDialog.setMessage("Uploading Files");
        progressDialog.show();
        String pdfFilePath = "questionPdfs/"+clickedCoursePassCode+"/"+testId+"/question";
        String markSheetPath = "markSheets/"+clickedCoursePassCode+"/"+testId+"/markSheet";
        if(txtViewUploadMarkSheetFileName.getVisibility() == View.GONE && txtViewUploadQuestionFileName.getVisibility() == View.VISIBLE){
            StorageReference storageReferencePdf = FirebaseStorage.getInstance().getReference(pdfFilePath);
            storageReferencePdf.putFile(pdfUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful());
                    String uploadedPdfUrl = ""+uriTask.getResult();

                    testHelper.setQuestion(uploadedPdfUrl);
                    TestHelperClass.createTest(TestCreationActivity.this, referenceTests, testHelper, testId);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull @NotNull Exception e) {
                    Toast.makeText(TestCreationActivity.this, "pdf upload failed due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        else if(txtViewUploadQuestionFileName.getVisibility() == View.GONE){
            TestHelperClass.createTest(this, referenceTests, testHelper, testId);
        }
        else{
            //markSheet and pdf both
            StorageReference storageReferencePdf = FirebaseStorage.getInstance().getReference(pdfFilePath);
            storageReferencePdf.putFile(pdfUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful());
                    String uploadedPdfUrl = ""+uriTask.getResult();

                    testHelper.setQuestion(uploadedPdfUrl);
                    //progressDialog.dismiss();

                    StorageReference storageReferenceCsv = FirebaseStorage.getInstance().getReference(markSheetPath);
                    storageReferenceCsv.putFile(markSheetUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());
                            String uploadedMarkSheetUrl = ""+uriTask.getResult();

                            testHelper.setMarkSheet(uploadedMarkSheetUrl);
                            progressDialog.dismiss();
                            testMarks = FileHelper.readCsv(markSheetUri, TestCreationActivity.this);
                            TestHelperClass.writeResultToDatabase(TestCreationActivity.this, clickedCoursePassCode, testMarks,testId);
                            TestHelperClass.createTest(TestCreationActivity.this, referenceTests, testHelper, testId);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull @NotNull Exception e) {
                            Toast.makeText(TestCreationActivity.this, "csv upload failed due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull @NotNull Exception e) {
                    Toast.makeText(TestCreationActivity.this, "pdf upload failed due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
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