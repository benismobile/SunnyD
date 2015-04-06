package org.fabeo.benbutchart.webmap;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.os.IBinder;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.MalformedJsonException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.MalformedInputException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class IODetectorService extends Service {

    public static String LOG_TAG = "IODetectorService";
    public static final  String CELL_INFO_ACTION = "org.fabeo.benbutchart.outdoorsy.CELL_INFO_ACTION" ;
    public static final  String CELL_INFO = "org.fabeo.benbutchart.outdoorsy.CELL_INFO" ;

    private TelephonyManager telephonyManager;
    private CellStateListener cellStateListener ;
    private WakefulAlarmBroadcastReceiver wakefulAlarmBroadcastReceiver ;
    private IODetectorSQLiteOpenHelper dbHelper ;


    public IODetectorService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(LOG_TAG, " onStartCommand") ;
          this.cellStateListener = new CellStateListener();
          this.telephonyManager = (TelephonyManager) this.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
         // this.telephonyManager.listen(cellStateListener, PhoneStateListener.LISTEN_CELL_INFO | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

         updateCellInfo();

        if(wakefulAlarmBroadcastReceiver==null) {wakefulAlarmBroadcastReceiver = new WakefulAlarmBroadcastReceiver() ; }


          if(intent == null) {
              Log.e(LOG_TAG, "intent passed to onStartCommand was null");
          }
          else

          {
              boolean wakeLockReleased = wakefulAlarmBroadcastReceiver.completeWakefulIntent(intent);
              Log.d(LOG_TAG, "Wake Lock released:" + wakeLockReleased);
          }


        // We can let the OS kill this service and not try to restart it as the wakeful alarm will start it again anyway
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy") ;
        this.telephonyManager.listen(cellStateListener, PhoneStateListener.LISTEN_NONE);

    }


    @Override
    public void onCreate() {

        super.onCreate();
        Log.d(LOG_TAG, "onCreate") ;
        dbHelper = new IODetectorSQLiteOpenHelper(this) ;
        Log.d(LOG_TAG, "dbHelper acquired") ;
    }



    private void updateCellInfo()
    {


        List<CellInfo> cellInfos = this.telephonyManager.getAllCellInfo();
        if(cellInfos==null)
        {
            Log.w(LOG_TAG, "Could not obtain cellInfos from telephonyManager") ;
            return ;
        }

        int cellIdHash = 0 ;
        int signalstrength = 0 ;

        Log.d(LOG_TAG, "update CellInfo: \n") ;

        int maxTimeT = dbHelper.getMaxTimeT() ;
        maxTimeT = maxTimeT + 1 ;

        ArrayList<Integer> currentCellIds = new ArrayList<Integer>(cellInfos.size()) ;

        for (CellInfo cellInfo : cellInfos) {

            if (cellInfo instanceof CellInfoGsm) {
                CellInfoGsm cellInfoGsm = (CellInfoGsm) cellInfo;
                CellIdentityGsm cellIdentity = cellInfoGsm.getCellIdentity();
                CellSignalStrengthGsm cellSignalStrengthGsm = cellInfoGsm.getCellSignalStrength();

             //   Log.d(LOG_TAG + "cell", " registered: " + cellInfoGsm.isRegistered());
             //   Log.d(LOG_TAG + "cell", cellIdentity.toString());
             //   Log.d(LOG_TAG + "cell", cellSignalStrengthGsm.toString());
                cellIdHash = cellIdentity.hashCode() ;
                signalstrength = cellSignalStrengthGsm.getDbm() ;

            } else if (cellInfo instanceof CellInfoCdma) {

                CellInfoCdma cellInfoCdma = (CellInfoCdma) cellInfo;
                CellIdentityCdma cellIdentity = cellInfoCdma.getCellIdentity();
                CellSignalStrengthCdma cellSignalStrengthCdma = cellInfoCdma.getCellSignalStrength();
          //      Log.d(LOG_TAG + "cell", "registered: " + cellInfoCdma.isRegistered());
           //     Log.d(LOG_TAG + "cell", cellIdentity.toString());
           //     Log.d(LOG_TAG + "cell", cellSignalStrengthCdma.toString());
                cellIdHash = cellIdentity.hashCode() ;
                signalstrength = cellSignalStrengthCdma.getDbm() ;

            } else if (cellInfo instanceof CellInfoWcdma) {
                CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) cellInfo;
                CellIdentityWcdma cellIdentity = cellInfoWcdma.getCellIdentity();
                CellSignalStrengthWcdma cellSignalStrengthWcdma = cellInfoWcdma.getCellSignalStrength();

               // Log.d(LOG_TAG + "cell", "registered: " + cellInfoWcdma.isRegistered());
               // Log.d(LOG_TAG + "cell", cellIdentity.toString());
               // Log.d(LOG_TAG + "cell", cellSignalStrengthWcdma.toString());
                cellIdHash = cellIdentity.hashCode() ;
                signalstrength = cellSignalStrengthWcdma.getDbm() ;

            } else if (cellInfo instanceof CellInfoLte) {
                CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
                CellIdentityLte cellIdentity = cellInfoLte.getCellIdentity();
                CellSignalStrengthLte cellSignalStrengthLte = cellInfoLte.getCellSignalStrength();


              //  Log.d(LOG_TAG + "cell", "registered: " + cellInfoLte.isRegistered());
              //  Log.d(LOG_TAG + "cell", cellIdentity.toString());
              //  Log.d(LOG_TAG + "cell", cellSignalStrengthLte.toString());
                cellIdHash = cellIdentity.hashCode() ;
                signalstrength = cellSignalStrengthLte.getDbm() ;

            }
            currentCellIds.add(cellIdHash) ;

            try {
                long numRows = dbHelper.insertCellInfo(maxTimeT, cellIdHash, signalstrength);

            }catch(java.sql.SQLException sqle) {
                    Log.w(LOG_TAG, "Could not insert cell info: " + sqle.getMessage() ) ;

            }catch(android.database.sqlite.SQLiteConstraintException sqliteCE)
            {
                Log.w(LOG_TAG, "Could not insert cell info: " + sqliteCE.getMessage() ) ;

            }


        }
        // ToDO delete cells no longer in range?

        // decrement timeT if we already have maximum time points in cellinfo table
        try {
            if(maxTimeT > 15) {
                dbHelper.decrementTimeT();
            }
        }catch(java.sql.SQLException sqle) {
            Log.w(LOG_TAG, "Could not decrement TimeT values: " + sqle.getMessage() ) ;

        }catch(android.database.sqlite.SQLiteConstraintException sqliteCE)
        {
            Log.w(LOG_TAG, "Could not decrement timeT values: " + sqliteCE.getMessage() ) ;

        }
        int minTimeT = dbHelper.getMinTimeT() ;

        // periodically get rid of old cellinfos
        if(minTimeT < -200)
        {

            dbHelper.deleteOldCellInfo() ;
        }


        List<String> stashedCellInfos = dbHelper.listCellInfo() ;
        String stashedvalues = "" ;




       // for(String stashedCellInfo : stashedCellInfos)
       // {
        //    stashedvalues = stashedvalues.concat(stashedCellInfo+ "\n") ;


       // }

       // Log.d(LOG_TAG, "stashed:\n" + stashedvalues) ;


        List<String> collatedCellInfos = dbHelper.collateCellInfo(null, 0) ;

        String collatedvalues = "" ;
        int numCellsSignalStrengthIncreased = 0 ;
        int numCellsSignalStrengthDecreased = 0 ;

        for(String collatedCellInfo : collatedCellInfos)
        {
            collatedvalues = collatedvalues.concat(collatedCellInfo+ "\n") ;


            //send broadcast message with collated cell info data
            // it should be picked up by MainActivity and displayed if listening

            Intent broadcastCellInfoIntent = new Intent() ;
            broadcastCellInfoIntent.setAction(CELL_INFO_ACTION);
            broadcastCellInfoIntent.putExtra(CELL_INFO, collatedvalues) ;
            this.sendBroadcast(broadcastCellInfoIntent);

            // TODO get the last  minimum in timeseries
            // TODO get the last maximum in timeseries
            // TODO OR just get first and last within timeseries
            // TODO OR just get min and max withing timeseries

            try {
                JSONObject jsonObject = new JSONObject(collatedCellInfo);
                JSONArray strengthValuesArray = jsonObject.getJSONArray("strengths");
                int numValues = strengthValuesArray.length() ;
                int firstSignalStrength = strengthValuesArray.getInt(0) ;
                int lastSignalStrength = strengthValuesArray.getInt(numValues - 1) ;
                int diff = lastSignalStrength - firstSignalStrength ;
                if(diff > 25)
                {
                    numCellsSignalStrengthIncreased += 1;
                }
                if(diff < -25)
                {
                    numCellsSignalStrengthDecreased += 1 ;
                }

            } catch (JSONException e) {
                Log.e(LOG_TAG, "Could not parse collated cellinfo object: " + e.getMessage() + " JSON input:" + collatedCellInfo) ;
                throw new IllegalArgumentException("Could not parse collated CellInfo JSON") ;
            }

        }
        Log.d(LOG_TAG, "\ncollated:\n" + collatedvalues) ;
        Log.d(LOG_TAG, "numCellsSignalStrengthIncreased:" + numCellsSignalStrengthIncreased );
        Log.d(LOG_TAG, "numCellsSignalStrengthDecreased:" + numCellsSignalStrengthDecreased );

        int numCellsDetected = collatedCellInfos.size() ;
        Log.d(LOG_TAG, "numCelsDetected:" + numCellsDetected) ;

        double correction = 0 ;
        //if(numCellsDetected==2) correction = 1 ;
        if(numCellsDetected==1) correction = 0.5 ;
        double confidenceOutdoorEnv = ( numCellsDetected == 0 ) ?  0.001 : (double) numCellsSignalStrengthIncreased / ((double)numCellsDetected+correction)  ;
        double confidenceIndoorEnv = ( numCellsDetected == 0 ) ?  0.001 : (double) numCellsSignalStrengthDecreased / ((double)numCellsDetected+correction)  ;
        Log.d(LOG_TAG, "confidenceOutdoors: " + confidenceOutdoorEnv  + "  confidenceIndoors:" + confidenceIndoorEnv) ;

        if(confidenceOutdoorEnv > 0.4) {

            Notification.Builder nBuilder = new Notification.Builder(this);
            nBuilder.setSmallIcon(R.drawable.map_blue)
                    .setContentTitle("IODetector:")
                            //             .setContentIntent(geofenceActivityPendingIntent)
                    .setVibrate(new long[]{0, 500})
                    .setContentText("Outdoors confidence" + confidenceOutdoorEnv);


            NotificationManager notificationMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationMgr.notify(12, nBuilder.build());
        }

        if(confidenceIndoorEnv > 0.4) {

            Notification.Builder nBuilder = new Notification.Builder(this);
            nBuilder.setSmallIcon(R.drawable.map_blue)
                    .setContentTitle("Geofence Alert")
                            //             .setContentIntent(geofenceActivityPendingIntent)
                    .setVibrate(new long[]{0, 500})
                    .setContentText("Indoors confidence" + confidenceIndoorEnv);


            NotificationManager notificationMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationMgr.notify(12, nBuilder.build());
        }

    }




    public class CellStateListener extends PhoneStateListener {


        @Override
        public void onCellInfoChanged(List<CellInfo> cellInfo) {


            if (cellInfo == null || cellInfo.iterator() == null) {
                Log.d(LOG_TAG, "onCellInfoChanged: no cellinfo data" + cellInfo);
                return;

            }

            for (Iterator<CellInfo> i = cellInfo.iterator(); i.hasNext(); ) {
                CellInfo cellInfo1 = i.next();
                String cellInfoDesc = cellInfo1.toString();
                Log.d(LOG_TAG, "onCellInfoChanged: " + cellInfoDesc);
            }

        }



        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            Log.d(LOG_TAG, " onSignalStrengthsChanged" + signalStrength.toString());
            updateCellInfo();
            }




    }
}