package com.example.zippy.helper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

public class EmailSender {
    @SuppressLint("IntentReset")
    public static void sendEmailTo(Activity activity, String[] emails){
        final Intent emailIntent = new Intent(android.content.Intent.ACTION_SENDTO);

        emailIntent.setType("message/rfc822");
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, emails);

        activity.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
    }
}
