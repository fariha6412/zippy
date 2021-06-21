package com.example.zippy.ui.test;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zippy.R;
import com.example.zippy.helper.InstructorHelperClass;
import com.example.zippy.helper.MenuHelperClass;
import com.example.zippy.helper.PassCodeGenerator;
import com.example.zippy.helper.TestHelperClass;
import com.example.zippy.helper.ValidationChecker;
import com.example.zippy.utility.NetworkChangeListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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
    DatabaseReference referenceTests;

    EditText editTXTTestTitle, editTXTTotalMarks, editTXTConvertTo;
    TextView txtViewUploadQuestionFileName, txtViewUploadMarkSheetFileName;
    Button questionUploadBtn, markSheetUploadBtn, submitBtn;
    private Uri pdfUri;
    private Uri markSheetUri;
    ArrayList<String> testIds;
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

        rootNode = FirebaseDatabase.getInstance();
        referenceTests = rootNode.getReference("tests/"+clickedCoursePassCode);

        questionUploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filePickIntent();
            }
        });
        markSheetUploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
                if(pdfUri == null || txtViewUploadQuestionFileName.getVisibility() == View.GONE){
                    new AlertDialog.Builder(TestCreationActivity.this)
                            .setTitle("Message")
                            .setMessage("You haven't uploaded any question")
                            .setPositiveButton("OKAY", null).create().show();
                }
                else {
                    filePickIntent();
                }
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

    private void filePickIntent(){
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
       startActivityForResult(Intent.createChooser(intent, "Select file"), PDF_PICK_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            Uri tempUri;
            if(requestCode == PDF_PICK_CODE || requestCode == CSV_PICK_CODE) {
                tempUri = data.getData();
                System.out.println(tempUri);
                String uriString = tempUri.toString();
                File myFile = new File(uriString);
                String path = myFile.getAbsolutePath();
                String displayName = null;

                if (uriString.startsWith("content://")) {
                    Cursor cursor = null;
                    try {
                        cursor = this.getContentResolver().query(tempUri, null, null, null, null);
                        if (cursor != null && cursor.moveToFirst()) {
                            displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                        }
                    } finally {
                        cursor.close();
                    }
                } else if (uriString.startsWith("file://")) {
                    displayName = myFile.getName();
                }
                if (requestCode == PDF_PICK_CODE) {
                    Pattern csvPattern = Pattern.compile(".+\\.pdf");
                    if(!csvPattern.matcher(displayName).matches()) {
                        Toast.makeText(TestCreationActivity.this, "Select a pdf file", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    pdfUri = tempUri;
                    txtViewUploadQuestionFileName.setText(displayName);
                    txtViewUploadQuestionFileName.setVisibility(View.VISIBLE);
                } else {
                    Pattern csvPattern = Pattern.compile(".+\\.csv");
                    if(!csvPattern.matcher(displayName).matches()) {
                        Toast.makeText(TestCreationActivity.this, "Select a csv file", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    markSheetUri = tempUri;
                    txtViewUploadMarkSheetFileName.setText(displayName);
                    txtViewUploadMarkSheetFileName.setVisibility(View.VISIBLE);
                }
            }
        }
        else {
            Toast.makeText(this, "Selection cancelled", Toast.LENGTH_SHORT).show();
        }

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
        String pdfFilePath = "questionPdfs/"+clickedCoursePassCode+"/"+testId;
        String markSheetPath = "markSheets/"+clickedCoursePassCode+"/"+testId;
        if(txtViewUploadMarkSheetFileName.getVisibility() == View.GONE && txtViewUploadQuestionFileName.getVisibility() == View.VISIBLE){
            StorageReference storageReferencePdf = FirebaseStorage.getInstance().getReference(pdfFilePath);
            storageReferencePdf.putFile(pdfUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful());
                    String uploadedPdfUrl = ""+uriTask.getResult();

                    testHelper.setQuestion(uploadedPdfUrl);

                    StorageReference storageReferenceCsv = FirebaseStorage.getInstance().getReference(pdfFilePath);
                    storageReferenceCsv.putFile(markSheetUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());
                            String uploadedCsvUrl = ""+uriTask.getResult();

                            testHelper.setMarkSheet(uploadedCsvUrl);
                            progressDialog.dismiss();

                            createTest(testHelper, testId);
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
        else if(txtViewUploadQuestionFileName.getVisibility() == View.GONE){
            createTest(testHelper, testId);
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
                    progressDialog.dismiss();

                    createTest(testHelper, testId);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull @NotNull Exception e) {
                    Toast.makeText(TestCreationActivity.this, "pdf upload failed due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    private void createTest(TestHelperClass testHelperClass, String testId){
        referenceTests.child(testId).setValue(testHelper, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable @org.jetbrains.annotations.Nullable DatabaseError error, @NonNull @NotNull DatabaseReference ref) {
                Toast.makeText(TestCreationActivity.this, "Test created successfully", Toast.LENGTH_SHORT).show();
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