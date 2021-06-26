package com.example.zippy.helper;

import android.app.Activity;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class TestHelperClass {
    private String testTitle;
    private String questionPdfUrl;
    private String markSheetCsvUrl;
    private Long totalMark;
    private Double convertTo;

    public TestHelperClass(){}

    public TestHelperClass(String testTitle, Long totalMarks, Double convertTo, String questionPdfUrl) {
        this.testTitle = testTitle;
        this.questionPdfUrl = questionPdfUrl;
        this.markSheetCsvUrl = "";
        this.totalMark = totalMarks;
        this.convertTo = convertTo;
    }

    public TestHelperClass(String testTitle, Long totalMark, Double convertTo) {
        this.testTitle = testTitle;
        this.totalMark = totalMark;
        this.convertTo = convertTo;
        this.questionPdfUrl = "";
        this.markSheetCsvUrl = "";
    }

    public String getTestTitle() {
        return testTitle;
    }

    public void setTestTitle(String testTitle) {
        this.testTitle = testTitle;
    }

    public String getQuestionPdfUrl() {
        return questionPdfUrl;
    }

    public void setQuestionPdfUrl(String questionPdfUrl) {
        this.questionPdfUrl = questionPdfUrl;
    }

    public String getMarkSheetCsvUrl() {
        return markSheetCsvUrl;
    }

    public void setMarkSheetCsvUrl(String markSheetCsvUrl) {
        this.markSheetCsvUrl = markSheetCsvUrl;
    }

    public Long getTotalMark() {
        return totalMark;
    }

    public void setTotalMark(Long totalMark) {
        this.totalMark = totalMark;
    }

    public Double getConvertTo() {
        return convertTo;
    }

    public void setConvertTo(Double convertTo) {
        this.convertTo = convertTo;
    }

    @Override
    public @NotNull String toString() {
        return "TestHelperClass{" +
                "testTitle='" + testTitle + '\'' +
                ", question='" + questionPdfUrl + '\'' +
                ", markSheet='" + markSheetCsvUrl + '\'' +
                ", totalMarks=" + totalMark +
                ", convertTo=" + convertTo +
                '}';
    }
    public static class TestMark{
        String regNo;
        Long totalMark;
        Double convertedMark;

        public TestMark(){}

        public TestMark(String regNo, Long totalMark, Double convertedMark) {
            this.regNo = regNo;
            this.totalMark = totalMark;
            this.convertedMark = convertedMark;
        }

        public String getRegNo() {
            return regNo;
        }

        public void setRegNo(String regNo) {
            this.regNo = regNo;
        }

        public Long getTotalMark() {
            return totalMark;
        }

        public void setTotalMark(Long totalMark) {
            this.totalMark = totalMark;
        }

        public Double getConvertedMark() {
            return convertedMark;
        }

        public void setConvertedMark(Double convertedMark) {
            this.convertedMark = convertedMark;
        }

        @Override
        public String toString() {
            return "TestMark{" +
                    "regNo='" + regNo + '\'' +
                    ", totalMark=" + totalMark +
                    ", convertedMark=" + convertedMark +
                    '}';
        }
    }
    public static void getTestList(Activity activity, ArrayList<String> testIds, ArrayList<String> testTitles, AutoCompleteTextView autoCompleteTextView, String clickedCoursePassCode){
        FirebaseDatabase rootNode = FirebaseDatabase.getInstance();
        DatabaseReference referenceTests = rootNode.getReference("tests/"+clickedCoursePassCode);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (activity, android.R.layout.select_dialog_item);

        referenceTests.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                testIds.clear();
                testTitles.clear();
                adapter.clear();
                for(DataSnapshot dsnap:snapshot.getChildren()){
                    testIds.add(dsnap.getKey());
                    adapter.add((String) dsnap.child("testTitle").getValue());
                    testTitles.add((String) dsnap.child("testTitle").getValue());
                }
                autoCompleteTextView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    public static void createTest(Activity activity, DatabaseReference referenceTests, TestHelperClass testHelper, String testId){
        referenceTests.child(testId).setValue(testHelper, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable @org.jetbrains.annotations.Nullable DatabaseError error, @NonNull @NotNull DatabaseReference ref) {
                Toast.makeText(activity, "Test created successfully", Toast.LENGTH_SHORT).show();
                System.out.println(testHelper.testTitle);
            }
        });
    }
}
