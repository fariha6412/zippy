package com.example.zippy.helper;

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

public class MenuHelperClass{
    Toolbar mtoolbar;
    Activity activity;

    public MenuHelperClass(Toolbar mtoolbar, Activity activity) {
        this.mtoolbar = mtoolbar;
        this.activity = activity;
    }

    public void handle(){
        mtoolbar.setOnMenuItemClickListener(this::onMenuItemClick);
    }

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
                        FirebaseAuth.getInstance().signOut();
                        context.startActivity(new Intent(context, MainActivity.class));
                    }
                }).create().show();
    }
}
