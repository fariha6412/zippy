package com.example.zippy.helper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ResultHelper {
    public static void writeTestResultToDatabase(Activity activity, String clickedCoursePassCode, ArrayList<TestHelper.TestMark> testMarks, String testId){
        ProgressDialog progressDialog;
        progressDialog = new ProgressDialog(activity);
        progressDialog.setTitle("wait");
        progressDialog.setMessage("Saving result");
        progressDialog.show();

        assert testMarks != null;
        HashMap<String, Long> totalMarks;
        HashMap<String, Double> convertedMarks;
        totalMarks = new HashMap<>();
        convertedMarks = new HashMap<>();
        for(TestHelper.TestMark tsm:testMarks){
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
                                    DatabaseReference referenceResult = FirebaseDatabase.getInstance().getReference("result/"+studentUid+"/"+clickedCoursePassCode+"/tests/"+testId);
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
    public static void getTotalMark(Activity activity, String coursePassCode, HashMap<Integer, String> gradingScaleHash, Double attendanceMark, Double totalMark){
        DatabaseReference referenceCourseStudentList = FirebaseDatabase.getInstance().getReference("courses/"+coursePassCode+"/students");
        referenceCourseStudentList.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for(DataSnapshot dsnap:snapshot.getChildren()){
                    String studentUID = (String) dsnap.getValue();
                    getTotalTestMark(activity, coursePassCode, studentUID, gradingScaleHash, attendanceMark, totalMark);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
    public static void getTotalTestMark(Activity activity, String coursePassCode, String studentUID, HashMap<Integer, String> gradingScaleHash, Double attendanceMark, Double totalMark){
        DatabaseReference referenceResult = FirebaseDatabase.getInstance().getReference("result/"+studentUID+"/"+coursePassCode+"/tests");
        referenceResult.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Double totalTestMark = 0.0;
                for(DataSnapshot dsnap:snapshot.getChildren()){
                    Object testMarkObj = dsnap.child("convertedMark").getValue();
                    Double testMark;

                    try {
                        testMark = (Double) testMarkObj;
                    }
                    catch (ClassCastException e){
                        testMark = 1.0*((Long) testMarkObj);
                    }
                    totalTestMark += testMark;
                }
                getAttendanceMark(activity, coursePassCode, studentUID, gradingScaleHash,totalTestMark, attendanceMark, totalMark);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
    public static void getAttendanceMark(Activity activity, String coursePassCode, String studentUID, HashMap<Integer, String> gradingScaleHash,Double totalTestMark, Double attendanceMark, Double totalMark){
        DatabaseReference referenceAttendance = FirebaseDatabase.getInstance().getReference("attendanceRecord/total/"+coursePassCode+"/"+studentUID);
        referenceAttendance.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                double percentage = 0.0;
                Double markGotOnAttendance = 0.0;
                if(snapshot.exists()){
                    Long totalPresent = (Long) snapshot.child("totalPresent").getValue();
                    Long totalAbsent = (Long) snapshot.child("totalAbsent").getValue();

                    if(totalAbsent == null || totalPresent == null){
                        percentage =  0.0;
                    }
                    else {
                        percentage = (totalPresent*100.0)/(totalAbsent+totalPresent);
                        markGotOnAttendance = (percentage*attendanceMark)/100.0;
                    }
                }
                HashMap<String, Double> attendance = new HashMap<>();
                attendance.put("percentage", percentage);
                attendance.put("got", markGotOnAttendance);
                DatabaseReference referenceResultedAttendance = FirebaseDatabase.getInstance().getReference("result/"+studentUID+"/"+coursePassCode);
                referenceResultedAttendance.child("attendance").setValue(attendance, (error, ref) -> {
                    //
                });
                Integer yourTotalMark = (int)Math.ceil(totalTestMark + markGotOnAttendance);
                String finalGrade = getGrade(yourTotalMark, gradingScaleHash);

                HashMap<String, String> finalEvaluation = new HashMap<>();
                finalEvaluation.put("totalMark", String.valueOf(totalMark));
                finalEvaluation.put("got", String.valueOf(yourTotalMark));
                finalEvaluation.put("finalGrade", finalGrade);
                DatabaseReference referenceFinalEvaluation = FirebaseDatabase.getInstance().getReference("result/"+studentUID+"/"+coursePassCode);
                referenceFinalEvaluation.child("finalEvaluation").setValue(finalEvaluation, (error, ref) -> {
                    //
                });
                DatabaseReference referenceCourse = FirebaseDatabase.getInstance().getReference("courses/"+coursePassCode);
                referenceCourse.child("isCompleted").setValue(true);
                String strIsCompleted = "isCompleted";
                SharedPreferences mPrefs;
                mPrefs = PreferenceManager.getDefaultSharedPreferences(activity);
                mPrefs.edit().putBoolean(strIsCompleted, true).apply();
                new AlertDialog.Builder(activity)
                        .setTitle("Message")
                        .setMessage("Make sure everything is okay. If you leave you won't be able to make any changes.")
                        .setNegativeButton("OKAY", null)
                        .create().show();
                getCourseTitleAndNotify(activity, coursePassCode);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
    private static void getCourseTitleAndNotify(Activity activity, String coursePassCode){
        DatabaseReference referenceCourseTitle = FirebaseDatabase.getInstance().getReference("courses/" + coursePassCode +"/courseTitle");
        referenceCourseTitle.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String courseTitle = snapshot.getValue().toString();
                    NotificationHelper.notifyAllStudents(activity, coursePassCode, "Course is completed", "Your course "+courseTitle+" is completed. Check the result.");
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
    public static void calculateAndStoreFinalResult(Activity activity, String coursePassCode, HashMap<Integer, String> gradingScaleHash, Double attendanceMark){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("tests/"+coursePassCode);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Double totalMark = attendanceMark;
                for(DataSnapshot dsnap:snapshot.getChildren()){
                    System.out.println(snapshot.getValue());
                    Object markObj = dsnap.child("convertTo").getValue();
                    Double markDbl;

                    try {
                        markDbl = (Double) markObj;
                    }
                    catch (ClassCastException e){
                        markDbl = ((Long) markObj)*1.0;
                    }
                    totalMark += markDbl;
                    System.out.println(markDbl);
                }
                System.out.println("total"+totalMark);
                Double highestGradeMark = getHighestGradeMark(gradingScaleHash)*1.0;
                System.out.println("highest"+highestGradeMark);
                getTotalMark(activity, coursePassCode, gradingScaleHash, attendanceMark, totalMark);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
    public static Integer getHighestGradeMark(HashMap<Integer, String> gradingScale){
        int ans = -10000;

        for (Map.Entry<Integer, String> integerStringEntry : gradingScale.entrySet()) {
            int temp = ((int) ((Map.Entry) integerStringEntry).getKey());
            if (temp > ans) {
                ans = temp;
            }
        }
        return ans;
    }
    public static String getGrade(Integer mark, HashMap<Integer, String> gradingScale){
        int ans = 10000;
        String strAns = null;

        for (Map.Entry<Integer, String> integerStringEntry : gradingScale.entrySet()) {
            int temp = ((int) ((Map.Entry) integerStringEntry).getKey());
            if (mark <= temp && temp < ans) {
                ans = temp;
                strAns = ((Map.Entry) integerStringEntry).getValue().toString();
            }
        }
        return strAns;
    }
}
