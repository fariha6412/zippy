package com.example.zippy.ui.test;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
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

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class TestDetailsActivity extends AppCompatActivity {
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    private static final int PDF_PICK_CODE = 1000;
    private static final int CSV_PICK_CODE = 1010;

    private DatabaseReference referenceResult, referenceTest;

    final String loggedStatus = "loggedProfile";
    final String strClickedCoursePassCode = "clickedCoursePassCode";
    private String clickedCoursePassCode;
    final String strClickedTestId = "clickedTestId";
    private String clickedTestId;
    private Uri pdfUri;
    private Uri markSheetUri;
    private String totalMark ="0";
    private String convertTo ="0";
    private String pdfFilePath;
    private String markSheetPath;

    private TextView txtViewUploadQuestionFileName, txtViewUploadMarkSheetFileName;
    private TextView txtViewTestTitle, txtViewTotalMarks, txtViewConvertTo, txtViewYourTotalMarks, txtViewAfterConversion;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_details);

        Toolbar toolbar = findViewById(R.id.mtoolbar);
        setSupportActionBar(toolbar);
        MenuHelperClass menuHelperClass = new MenuHelperClass(toolbar, this);
        menuHelperClass.handle();

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String loggedProfile = mPrefs.getString(loggedStatus, "nouser");
        clickedCoursePassCode = mPrefs.getString(strClickedCoursePassCode, "");
        clickedTestId = mPrefs.getString(strClickedTestId, "");

        referenceTest = FirebaseDatabase.getInstance().getReference("tests/"+clickedCoursePassCode+"/"+clickedTestId);
        pdfFilePath = "questionPdfs/"+clickedCoursePassCode+"/"+clickedTestId+"/question";
        markSheetPath = "markSheets/"+clickedCoursePassCode+"/"+clickedTestId+"/markSheet";

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
            referenceResult = FirebaseDatabase.getInstance().getReference("result/"+ user.getUid()+"/"+clickedCoursePassCode+"/"+clickedTestId);
        }

        txtViewTotalMarks.setText(totalMark);
        txtViewConvertTo.setText(convertTo);
        String yourTotalMark = "0";
        txtViewYourTotalMarks.setText(yourTotalMark);
        String afterConversion = "0";
        txtViewAfterConversion.setText(afterConversion);

        getTestDetails();
        if(loggedProfile.equals("student"))getResult();
        uploadQuestionBtn.setOnClickListener(v -> {
            FileHelper.filePickIntent(this, PDF_PICK_CODE);
        });
        uploadMarkSheetBtn.setOnClickListener(v -> {
            checkHasQuestion();
        });
        downloadQuestionBtn.setOnClickListener(v -> {
            downloadFile(pdfFilePath, "question", ".pdf");
        });
        downloadMarkSheetBtn.setOnClickListener(v -> {
            downloadFile(markSheetPath, "markSheet", ".csv");
        });
    }
    private void downloadFile(String path, String fileName, String extension){
        ProgressDialog progressDialog = new ProgressDialog(TestDetailsActivity.this);
        progressDialog.setTitle("wait");
        progressDialog.setMessage("Downloading file");
        progressDialog.show();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(path);
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                DownloadManager downloadManager = (DownloadManager) TestDetailsActivity.this.getSystemService(Context.DOWNLOAD_SERVICE);
                DownloadManager.Request request = new DownloadManager.Request(uri);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalFilesDir(TestDetailsActivity.this, DIRECTORY_DOWNLOADS, fileName + extension);
                downloadManager.enqueue(request);
                progressDialog.dismiss();
            }
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(TestDetailsActivity.this, "file download failed due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
    public void checkHasQuestion(){
        referenceTest.child("question").addListenerForSingleValueEvent(new ValueEventListener() {
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
                referenceTest.child("question").setValue(uploadedPdfUrl);
                Toast.makeText(TestDetailsActivity.this, "Uploaded successfully", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(TestDetailsActivity.this, "pdf upload failed due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
    private void uploadMarkSheetToDatabase(){
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
            ArrayList<TestHelperClass.TestMark> testMarks = new ArrayList<>();
            testMarks = FileHelper.readCsv(markSheetUri, TestDetailsActivity.this);
            System.out.println(testMarks);
            assert testMarks != null;
            TestHelperClass.writeResultToDatabase(TestDetailsActivity.this, clickedCoursePassCode, testMarks,clickedTestId);

            referenceTest.child("markSheet").setValue(uploadedMarkSheetUrl);
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(TestDetailsActivity.this, "pdf upload failed due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
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