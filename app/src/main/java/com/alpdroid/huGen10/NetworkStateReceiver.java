package com.alpdroid.huGen10;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetworkStateReceiver extends BroadcastReceiver {

    private static final String TAG = NetworkStateReceiver.class.getName();
    private final AlpdroidEr alpdroidEr;

    public NetworkStateReceiver(AlpdroidEr alpdroidEr) {this.alpdroidEr = alpdroidEr;}

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getExtras() != null) {
            NetworkInfo networkInfo =
                    (NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);

            if (networkInfo != null && networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                Log.d(TAG, "Network connected, scrobbling");
     //           alpdroidEr.alpdroidPending();
            }
        }
    }
}