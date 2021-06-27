package com.example.zippy.helper;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.zippy.MainActivity;
import com.example.zippy.R;
import com.example.zippy.ui.course.CourseDetailsActivity;
import com.example.zippy.ui.test.TestDetailsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

public class NotificationHelper{
    public static void notifyUser(String uid, String title, String body, Activity activity){
        FirebaseDatabase.getInstance().getReference("userTokens").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String token = (String) snapshot.getValue();
                FcmNotificationsSender fcmNotificationsSender = new FcmNotificationsSender(token, title, body, activity.getApplicationContext(), activity);
                fcmNotificationsSender.SendNotifications();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    public static void notifyAllStudents(Activity activity, String coursePassCode, String title, String body) {
        FirebaseDatabase.getInstance().getReference("courses/"+coursePassCode+"/students").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for(DataSnapshot dsnap:snapshot.getChildren()){
                    String uid = (String) dsnap.getKey();
                    FirebaseDatabase.getInstance().getReference("userTokens/"+uid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapt) {
                            String token = (String)snapt.getValue();
                            NotificationHelper.notifyUser(token, title, body, activity);
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
}