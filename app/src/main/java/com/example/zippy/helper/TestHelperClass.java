package com.example.zippy.helper;

import android.app.Activity;
import android.app.ProgressDialog;
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
import java.util.HashMap;

public class TestHelperClass {
    String testTitle;
    String questionPdfUrl;
    String markSheet;
    Long totalMark;
    Double convertTo;

    public TestHelperClass(){}

    public TestHelperClass(String testTitle, Long totalMarks, Double convertTo, String question) {
        this.testTitle = testTitle;
        this.questionPdfUrl = question;
        this.markSheet = "";
        this.totalMark = totalMarks;
        this.convertTo = convertTo;
    }

    public TestHelperClass(String testTitle, Long totalMark, Double convertTo) {
        this.testTitle = testTitle;
        this.totalMark = totalMark;
        this.convertTo = convertTo;
        this.questionPdfUrl = "";
        this.markSheet = "";
    }

    public String getTestTitle() {
        return testTitle;
    }

    public void setTestTitle(String testTitle) {
        this.testTitle = testTitle;
    }

    public String getQuestion() {
        return questionPdfUrl;
    }

    public void setQuestion(String question) {
        this.questionPdfUrl = question;
    }

    public String getMarkSheet() {
        return markSheet;
    }

    public void setMarkSheet(String markSheet) {
        this.markSheet = markSheet;
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
                ", markSheet='" + markSheet + '\'' +
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
        referenceTests.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
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
    public static void writeResultToDatabase(Activity activity, String clickedCoursePassCode, ArrayList <TestHelperClass.TestMark> testMarks, String testId){
        ProgressDialog progressDialog;
        progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage("Saving result");
        progressDialog.show();

        assert testMarks != null;
        HashMap<String, Long> totalMarks;
        HashMap<String, Double> convertedMarks;
        totalMarks = new HashMap<>();
        convertedMarks = new HashMap<>();
        for(TestHelperClass.TestMark tsm:testMarks){
            totalMarks.put(tsm.getRegNo(), tsm.getTotalMark());
            convertedMarks.put(tsm.getRegNo(), tsm.getConvertedMark());
        }
        DatabaseReference referenceStudentList = FirebaseDatabase.getInstance().getReference("courses/"+clickedCoursePassCode+"/students");

        referenceStudentList.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot dsnap:snapshot.getChildren()){
                        String studentUid = (String) dsnap.getValue();

                        DatabaseReference referenceStudentRegNo = FirebaseDatabase.getInstance().getReference("students/"+studentUid+"/registrationNo");

                        referenceStudentRegNo.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()){
                                    String registrationNo = (String) dataSnapshot.getValue();
                                    Long totalMark = totalMarks.get(registrationNo);
                                    Double convertedMark = convertedMarks.get(registrationNo);
                                    DatabaseReference referenceResult = FirebaseDatabase.getInstance().getReference("result/"+studentUid+"/"+clickedCoursePassCode+"/"+testId);
                                    referenceResult.child("totalMark").setValue(totalMark);
                                    referenceResult.child("convertedMark").setValue(convertedMark);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                            }
                        });
                    }
                    progressDialog.dismiss();
                    Toast.makeText(activity, "Saved successfully", Toast.LENGTH_SHORT).show();
                }
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
            }
        });
    }
}
