package com.example.zippy.helper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;

import com.example.zippy.R;
import com.example.zippy.ui.test.TestDetailsActivity;
import com.example.zippy.utility.common;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.opencsv.CSVReader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class FileHelper {
    private static final int PDF_PICK_CODE = 1000;
    private static final int CSV_PICK_CODE = 1010;

    public static ArrayList<TestHelperClass.TestMark> readMarkSheetCsv(Uri uri, Context context) {
            try {
                CSVReader reader = new CSVReader(new InputStreamReader(new BufferedInputStream(context.getContentResolver().openInputStream(uri))));
                String [] nextLine;
                ArrayList<TestHelperClass.TestMark> testMarks = new ArrayList<>();
                Boolean flag = true;

                while ((nextLine = reader.readNext()) != null)
                {
                    int i = 0;
                    TestHelperClass.TestMark testMark = new TestHelperClass.TestMark();
                    for(String token : nextLine)
                    {
                        if(i==0)testMark.setRegNo(token);
                        if(i==1)testMark.setTotalMark(Long.parseLong(token));
                        if(i==2)testMark.setConvertedMark(Double.parseDouble(token));
                        i++;
                    }
                    if(testMark.getRegNo()==null || testMark.getTotalMark()==null || testMark.getConvertedMark()==null || i!=3){
                        flag = false;
                        break;
                    }
                    testMarks.add(testMark);
                }
                if(flag) return testMarks;
            } catch (Exception e) {
                String warningDetails = "Please upload a csv file with three columns [registrationNo, totalMark, convertedMark]";
                alertForCsvFormat(context, warningDetails);
            }
        return null;
    }
    public static void filePickIntent(Activity activity, int code){
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activity.startActivityForResult(Intent.createChooser(intent, "Select file"), code);
    }

    public static Uri findPickedFileUri(Activity activity, TextView txtView, int requestCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        Uri tempUri;
        if(requestCode == PDF_PICK_CODE || requestCode == CSV_PICK_CODE) {
            assert data != null;
            tempUri = data.getData();
            System.out.println(tempUri);
            String uriString = tempUri.toString();
            File myFile = new File(uriString);
            String path = myFile.getAbsolutePath();
            String displayName = null;

            if (uriString.startsWith("content://")) {
                Cursor cursor = null;
                try {
                    cursor = activity.getContentResolver().query(tempUri, null, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    }
                } finally {
                    assert cursor != null;
                    cursor.close();
                }
            } else if (uriString.startsWith("file://")) {
                displayName = myFile.getName();
            }
            if (requestCode == PDF_PICK_CODE) {
                Pattern csvPattern = Pattern.compile(".+\\.pdf");
                if(!csvPattern.matcher(displayName).matches()) {
                    Toast.makeText(activity, "Select a pdf file", Toast.LENGTH_SHORT).show();
                    return null;
                }
                if(txtView!=null) {
                    txtView.setText(displayName);
                    txtView.setVisibility(View.VISIBLE);
                }
                return tempUri;
            } else {
                Pattern csvPattern = Pattern.compile(".+\\.csv");
                if(!csvPattern.matcher(displayName).matches()) {
                    Toast.makeText(activity, "Select a csv file", Toast.LENGTH_SHORT).show();
                    return null;
                }
                if(txtView!=null) {
                    txtView.setText(displayName);
                    txtView.setVisibility(View.VISIBLE);
                }
                return tempUri;
            }
        }
        return null;
    }
    public static void alertForCsvFormat(Context context, String warningDetails)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View layout_dialog = LayoutInflater.from(context).inflate(R.layout.csv_format_warning, null);
        builder.setView(layout_dialog);
        AppCompatTextView textViewDetails = layout_dialog.findViewById(R.id.warningDetails);
        textViewDetails.setText(warningDetails);
        AppCompatButton btnOkay = layout_dialog.findViewById(R.id.btnOkay);

        //show dialog
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.setCancelable(false);

        dialog.getWindow().setGravity(Gravity.CENTER);
        btnOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }
    public static void downloadFile(Activity activity, String path, String fileName, String extension){
        ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setTitle("wait");
        progressDialog.setMessage("Downloading file");
        progressDialog.show();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(path);
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                DownloadManager downloadManager = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
                DownloadManager.Request request = new DownloadManager.Request(uri);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalFilesDir(activity, DIRECTORY_DOWNLOADS, fileName + extension);
                downloadManager.enqueue(request);
                progressDialog.dismiss();
            }
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(activity, "file download failed due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
    public static HashMap<Integer, String> readGradingScaleCsv(Uri uri, Context context) {
        try {
            CSVReader reader = new CSVReader(new InputStreamReader(new BufferedInputStream(context.getContentResolver().openInputStream(uri))));
            String [] nextLine;
            HashMap<Integer, String> gradingScale = new HashMap<>();

            while ((nextLine = reader.readNext()) != null)
            {
                gradingScale.put(Integer.parseInt(nextLine[0]), nextLine[1]);
            }
            return gradingScale;
        } catch (Exception e) {
            String warningDetails = "Please upload a csv file with two columns [upper limit of mark, grade]";
            alertForCsvFormat(context, warningDetails);
        }
        return null;
    }
}
