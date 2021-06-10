package com.example.zippy.utility;

import android.content.Context;
import android.net.ConnectivityManager;

import android.net.Network;
import android.net.NetworkInfo;

public class common {
    public static boolean isConnectedToInternet(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            Network[] networks = connectivityManager.getAllNetworks();
            for(Network nt:networks){
                NetworkInfo info = connectivityManager.getNetworkInfo(nt);
                if (info.getState() == NetworkInfo.State.CONNECTED)
                    return true;
            }
            /*NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                        return true;
                }
            }*/
        }
        return false;
    }
}
