package org.fabeo.benbutchart.webmap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

public class WakefulAlarmBroadcastReceiver extends WakefulBroadcastReceiver {

    public static final String LOG_TAG = "WakefulBroadcastReceiver" ;

    public WakefulAlarmBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(LOG_TAG, "onReceive") ;
        Intent startIODetectorIntent = new Intent(context, IODetectorService.class);
        //startIODetectorIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //context.startService(startIODetectorIntent) ;


        // start IODetector service keeping device awake
        startWakefulService(context, startIODetectorIntent);


    }
}
