package com.example.zippy.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.zippy.ui.test.TestDetailsActivity;
import com.opencsv.CSVReader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class FileHelper {
    private static final int PDF_PICK_CODE = 1000;
    private static final int CSV_PICK_CODE = 1010;

    public static ArrayList<TestHelperClass.TestMark> readCsv(Uri uri, Context context) {
            try {
                CSVReader reader = new CSVReader(new InputStreamReader(new BufferedInputStream(context.getContentResolver().openInputStream(uri))));
                String [] nextLine;
                ArrayList<TestHelperClass.TestMark> testMarks = new ArrayList<>();

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
                    testMarks.add(testMark);
                }
                return testMarks;
            } catch (IOException e) {
                e.printStackTrace();
            }
        return null;
    }
    public static void filePickIntent(Activity activity, int code){
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activity.startActivityForResult(Intent.createChooser(intent, "Select file"), code);
    }

    public static Uri onActivityResult(Activity activity, TextView txtView, int requestCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
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
                txtView.setText(displayName);
                txtView.setVisibility(View.VISIBLE);
                return tempUri;
            } else {
                Pattern csvPattern = Pattern.compile(".+\\.csv");
                if(!csvPattern.matcher(displayName).matches()) {
                    Toast.makeText(activity, "Select a csv file", Toast.LENGTH_SHORT).show();
                    return null;
                }
                txtView.setText(displayName);
                txtView.setVisibility(View.VISIBLE);
                return tempUri;
            }
        }
        return null;
    }
}
