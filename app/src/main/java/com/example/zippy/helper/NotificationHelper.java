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
    public static void notify(Activity activity, String notificationTitle, String notificationMessage){
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(activity)
                        .setSmallIcon(R.drawable.ic_baseline_notification_important_24)
                        .setContentTitle(notificationTitle)
                        .setContentText(notificationMessage);

        Intent notificationIntent = new Intent(activity, CourseDetailsActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(activity, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        NotificationManager manager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            String channelId = "Your_channel_id";
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
            builder.setChannelId(channelId);
        }
        manager.notify(0, builder.build());
    }
    public static void testMarkGivenNotification(FirebaseUser user, Activity activity){
        DatabaseReference referenceCourseList;
        FirebaseDatabase rootNode;
        rootNode = FirebaseDatabase.getInstance();
        referenceCourseList = rootNode.getReference("students/"+user.getUid()+"/courses");
        System.out.println("baire");

        referenceCourseList.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for(DataSnapshot dsanp:snapshot.getChildren()){
                    String coursePassCode = dsanp.getKey();
                    assert coursePassCode != null;

                    final String[] courseTitle = new String[1];
                    DatabaseReference referenceCourse = rootNode.getReference("courses/"+coursePassCode);
                    referenceCourse.child("courseTitle").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot dsnapshot) {
                            if(dsnapshot.exists()) {
                                courseTitle[0] = (String) dsnapshot.getValue();
                                DatabaseReference referenceResult;
                                referenceResult = rootNode.getReference("result/"+user.getUid()+"/"+coursePassCode);

                                referenceResult.addChildEventListener(new ChildEventListener() {
                                    @Override
                                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                        if(dataSnapshot.exists()) {
                                            NotificationHelper.notify(activity, "Notification", "Your test mark is given for "+ courseTitle[0]);
                                        }
                                    }
                                    @Override
                                    public void onChildChanged(DataSnapshot dataSnapshot, String s)
                                    {
                                        if(dataSnapshot.exists()) {
                                            NotificationHelper.notify(activity, "Notification", "Your test mark is updated for "+ courseTitle[0]);
                                        }
                                    }
                                    @Override
                                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                                    }

                                    @Override
                                    public void onChildMoved(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {

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

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
}