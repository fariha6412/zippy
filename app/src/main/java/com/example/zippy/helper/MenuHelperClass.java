package com.example.zippy.helper;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.appcompat.app.AlertDialog;

import com.example.zippy.AboutActivity;
import com.example.zippy.MainActivity;
import com.google.firebase.auth.FirebaseAuth;

public class MenuHelperClass {
    public static void showAbout(Activity context){
        context.startActivity(new Intent(context, AboutActivity.class));
    }
    public static void exit(Activity context) {
        new AlertDialog.Builder(context)
                .setTitle("Message")
                .setMessage("Do you want to exit app?")
                .setNegativeButton("NO", null)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        System.exit(0);
                    }
                }).create().show();
    }
    public static void signOut(Activity context){
        new AlertDialog.Builder(context)
                .setTitle("Message")
                .setMessage("Do you want to log out?")
                .setNegativeButton("NO", null)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        FirebaseAuth.getInstance().signOut();
                        context.startActivity(new Intent(context, MainActivity.class));
                    }
                }).create().show();
    }
}
