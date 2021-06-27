package com.example.zippy.helper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.MenuItem;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import com.example.zippy.ui.extras.AboutActivity;
import com.example.zippy.MainActivity;
import com.example.zippy.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class MenuHelperClass{
    Toolbar mtoolbar;
    Activity activity;

    public MenuHelperClass(Toolbar toolbar, Activity activity) {
        this.mtoolbar = toolbar;
        this.activity = activity;
    }

    public void handle(){
        mtoolbar.setOnMenuItemClickListener(this::onMenuItemClick);
    }

    @SuppressLint("NonConstantResourceId")
    public boolean onMenuItemClick(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menuabout:
                MenuHelperClass.showAbout(activity);
                break;
            case R.id.menuexit:
                MenuHelperClass.exit(activity);
                break;
            case R.id.menulogout:
                MenuHelperClass.signOut(activity);
                break;
        }
        return true;
    }
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
                        context.finishAffinity();
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
                        FirebaseDatabase.getInstance().getReference("userTokens").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).removeValue();
                        FirebaseAuth.getInstance().signOut();
                        context.startActivity(new Intent(context, MainActivity.class));
                    }
                }).create().show();
    }
}
