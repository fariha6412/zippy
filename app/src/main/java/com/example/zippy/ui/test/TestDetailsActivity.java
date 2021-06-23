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
import com.example.zippy.helper.TestHelperClass;
import com.example.zippy.utility.NetworkChangeListener;
import com.google.android.gms.tasks.OnFailureListener;
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

    DatabaseReference referenceResult, referenceTest;
    FirebaseUser user;

    SharedPreferences mPrefs;
    final String loggedStatus = "loggedProfile";
    final String strClickedCoursePassCode = "clickedCoursePassCode";
    String clickedCoursePassCode;
    final String strClickedTestId = "clickedTestId";
    String clickedTestId;
    private Uri pdfUri;
    private Uri markSheetUri;
    private String totalMark ="0", yourTotalMark ="0", convertTo ="0", afterConversion ="0";

    private TextView txtViewUploadQuestionFileName, txtViewUploadMarkSheetFileName;
    private TextView txtViewTestTitle, txtViewTotalMarks, txtViewConvertTo, txtViewYourTotalMarks, txtViewAfterConversion;
    private LinearLayout yourTotalMarkLinearLayout, afterConversionLinearLayout, markSheetLinearLayout;
    private MaterialButton editTotalMarkBtn, editConvertToBtn, uploadQuestionBtn, downloadQuestionBtn, downloadMarkSheetBtn, uploadMarkSheetBtn;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_details);

        Toolbar mtoolbar = findViewById(R.id.mtoolbar);
        setSupportActionBar(mtoolbar);
        MenuHelperClass menuHelperClass = new MenuHelperClass(mtoolbar, this);
        menuHelperClass.handle();

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String loggedProfile = mPrefs.getString(loggedStatus, "nouser");
        clickedCoursePassCode = mPrefs.getString(strClickedCoursePassCode, "");
        clickedTestId = mPrefs.getString(strClickedTestId, "");

        referenceTest = FirebaseDatabase.getInstance().getReference("tests/"+clickedCoursePassCode+"/"+clickedTestId);

        txtViewTotalMarks = findViewById(R.id.txtviewtotalmark);
        txtViewConvertTo = findViewById(R.id.txtviewconvertto);
        txtViewYourTotalMarks = findViewById(R.id.txtviewyourtotalmark);
        txtViewAfterConversion = findViewById(R.id.txtviewafterconvertion);

        txtViewTestTitle = findViewById(R.id.txtviewtesttitle);

        txtViewUploadQuestionFileName = findViewById(R.id.uploadquestionfilename);
        txtViewUploadMarkSheetFileName = findViewById(R.id.uploadmarksheetfilename);

        yourTotalMarkLinearLayout = findViewById(R.id.yourTotalMarkLinearLayout);
        afterConversionLinearLayout = findViewById(R.id.afterConversionLinearLayout);
        markSheetLinearLayout = findViewById(R.id.markSheetLinearLayout);

        editTotalMarkBtn = findViewById(R.id.chgtotalmarksbtn);
        editConvertToBtn = findViewById(R.id.chgconverttobtn);
        uploadQuestionBtn = findViewById(R.id.uploadquestionbtn);
        uploadMarkSheetBtn = findViewById(R.id.uploadmarksheetbtn);
        downloadQuestionBtn = findViewById(R.id.downloadquestionbtn);
        downloadMarkSheetBtn = findViewById(R.id.downloadmarksheetbtn);

        if(loggedProfile.equals("instructor")){
            yourTotalMarkLinearLayout.setVisibility(View.GONE);
            afterConversionLinearLayout.setVisibility(View.GONE);
        }
        if(loggedProfile.equals("student")){
            markSheetLinearLayout.setVisibility(View.GONE);
            editConvertToBtn.setVisibility(View.GONE);
            editTotalMarkBtn.setVisibility(View.GONE);
            uploadQuestionBtn.setVisibility(View.GONE);
            user = FirebaseAuth.getInstance().getCurrentUser();
            referenceResult = FirebaseDatabase.getInstance().getReference("result/"+user.getUid()+"/"+clickedCoursePassCode+"/"+clickedTestId);
        }

        txtViewTotalMarks.setText(totalMark);
        txtViewConvertTo.setText(convertTo);
        txtViewYourTotalMarks.setText(yourTotalMark);
        txtViewAfterConversion.setText(afterConversion);

        getTestDetails();
        if(loggedProfile.equals("student"))getResult();
        uploadQuestionBtn.setOnClickListener(v -> {
            FileHelper.filePickIntent(this, PDF_PICK_CODE);
        });
        uploadMarkSheetBtn.setOnClickListener(v -> {
            checkHasQuestion();
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
    public void checkHasQuestion(){
        referenceTest.child("question").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String questionUrl = (String) snapshot.getValue();
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
                pdfUri = FileHelper.onActivityResult(this, txtViewUploadQuestionFileName, requestCode, data );
                if(pdfUri != null)uploadPdfToDatabase();
            }
            else {
                markSheetUri = FileHelper.onActivityResult(this, txtViewUploadMarkSheetFileName, requestCode, data );
                if(markSheetUri != null)uploadMarkSheetToDatabase();
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
        progressDialog.setMessage("Uploading PDF");
        progressDialog.show();
        String pdfFilePath = "questionPdfs/"+clickedCoursePassCode+"/"+clickedTestId+"/question";
        StorageReference storageReferencePdf = FirebaseStorage.getInstance().getReference(pdfFilePath);
        storageReferencePdf.putFile(pdfUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                String uploadedPdfUrl = ""+uriTask.getResult();
                progressDialog.dismiss();
                referenceTest.child("question").setValue(uploadedPdfUrl);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                Toast.makeText(TestDetailsActivity.this, "pdf upload failed due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void uploadMarkSheetToDatabase(){
        ProgressDialog progressDialog = new ProgressDialog(TestDetailsActivity.this);
        progressDialog.setMessage("Uploading CSV");
        progressDialog.show();
        String markSheetPath = "markSheets/"+clickedCoursePassCode+"/"+clickedTestId+"/markSheet";
        StorageReference storageReferenceCsv = FirebaseStorage.getInstance().getReference(markSheetPath);
        storageReferenceCsv.putFile(markSheetUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                String uploadedMarkSheetUrl = ""+uriTask.getResult();

                progressDialog.dismiss();
                ArrayList<TestHelperClass.TestMark> testMarks = new ArrayList<>();
                testMarks = FileHelper.readCsv(markSheetUri, TestDetailsActivity.this);
                System.out.println(testMarks);
                assert testMarks != null;
                TestHelperClass.writeResultToDatabase(TestDetailsActivity.this, clickedCoursePassCode, testMarks,clickedTestId);

                referenceTest.child("markSheet").setValue(uploadedMarkSheetUrl);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                Toast.makeText(TestDetailsActivity.this, "pdf upload failed due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
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