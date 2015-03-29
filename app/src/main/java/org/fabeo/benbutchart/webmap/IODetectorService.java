package org.fabeo.benbutchart.webmap;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
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

import java.util.Iterator;
import java.util.List;

public class IODetectorService extends Service {

    public static String LOG_TAG = "IODetectorService";


    private TelephonyManager telephonyManager;
    private Intent intent ;

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
          CellStateListener cellStateListener = new CellStateListener();
          this.telephonyManager = (TelephonyManager) this.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
          this.telephonyManager.listen(cellStateListener, PhoneStateListener.LISTEN_CELL_INFO | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
          WakefulAlarmBroadcastReceiver.completeWakefulIntent(intent);

       //     checkCells();
        //    WakefulAlarmBroadcastReceiver.completeWakefulIntent(intent) ;

        //Notification noti = new Notification.Builder(this)
         //       .setContentTitle("Outside Detector")
         //       .setContentText("Outside Detector is running")
         //       .setSmallIcon(R.drawable.ic_launcher)
         //       .build();

       // startForeground(223344, noti);

        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy") ;
        super.onDestroy();

    }

    protected void completeWakefulIntent() {
        WakefulAlarmBroadcastReceiver.completeWakefulIntent(intent);
    }

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "onCreate") ;
        super.onCreate();
    }

    private void checkCells()
    {

        List<CellInfo> cellInfos = telephonyManager.getAllCellInfo();

        for (CellInfo cellInfo : cellInfos) {

            if (cellInfo instanceof CellInfoGsm) {
                CellInfoGsm cellInfoGsm = (CellInfoGsm) cellInfo;
                CellIdentityGsm cellIdentity = cellInfoGsm.getCellIdentity();
                CellSignalStrengthGsm cellSignalStrengthGsm = cellInfoGsm.getCellSignalStrength();

                Log.d(LOG_TAG + "cell", "registered: " + cellInfoGsm.isRegistered());
                Log.d(LOG_TAG + "cell", cellIdentity.toString());
                Log.d(LOG_TAG + "cell", cellSignalStrengthGsm.toString());
            } else if (cellInfo instanceof CellInfoCdma) {

                CellInfoCdma cellInfoCdma = (CellInfoCdma) cellInfo;
                CellIdentityCdma cellIdentity = cellInfoCdma.getCellIdentity();
                CellSignalStrengthCdma cellSignalStrengthCdma = cellInfoCdma.getCellSignalStrength();
                Log.d(LOG_TAG + "cell", "registered: " + cellInfoCdma.isRegistered());
                Log.d(LOG_TAG + "cell", cellIdentity.toString());
                Log.d(LOG_TAG + "cell", cellSignalStrengthCdma.toString());
            } else if (cellInfo instanceof CellInfoWcdma) {
                CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) cellInfo;
                CellIdentityWcdma cellIdentity = cellInfoWcdma.getCellIdentity();
                CellSignalStrengthWcdma cellSignalStrengthWcdma = cellInfoWcdma.getCellSignalStrength();

                Log.d(LOG_TAG + "cell", "registered: " + cellInfoWcdma.isRegistered());
                Log.d(LOG_TAG + "cell", cellIdentity.toString());
                Log.d(LOG_TAG + "cell", cellSignalStrengthWcdma.toString());

            } else if (cellInfo instanceof CellInfoLte) {
                CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
                CellIdentityLte cellIdentity = cellInfoLte.getCellIdentity();
                CellSignalStrengthLte cellSignalStrengthLte = cellInfoLte.getCellSignalStrength();


                Log.d(LOG_TAG + "cell", "registered: " + cellInfoLte.isRegistered());
                Log.d(LOG_TAG + "cell", cellIdentity.toString());
                Log.d(LOG_TAG + "cell", cellSignalStrengthLte.toString());


            }
        }

    }

    public class CellStateListener extends PhoneStateListener {


        @Override
        public void onCellInfoChanged(List<CellInfo> cellInfo) {
            super.onCellInfoChanged(cellInfo);

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
            List<CellInfo> cellInfos = telephonyManager.getAllCellInfo();

            for (Iterator<CellInfo> i = cellInfos.iterator(); i.hasNext(); ) {
                CellInfo cellInfo1 = i.next();

                String cellInfoStr = cellInfo1.toString();

                Log.d(LOG_TAG, " onSignalStrengthsChanged cellInfo:" + cellInfoStr);
            }

            for (CellInfo cellInfo : cellInfos) {

                if (cellInfo instanceof CellInfoGsm) {
                    CellInfoGsm cellInfoGsm = (CellInfoGsm) cellInfo;
                    CellIdentityGsm cellIdentity = cellInfoGsm.getCellIdentity();
                    CellSignalStrengthGsm cellSignalStrengthGsm = cellInfoGsm.getCellSignalStrength();

                    Log.d(LOG_TAG + "cell", "registered: " + cellInfoGsm.isRegistered());
                    Log.d(LOG_TAG + "cell", cellIdentity.toString());
                    Log.d(LOG_TAG + "cell", cellSignalStrengthGsm.toString());
                } else if (cellInfo instanceof CellInfoCdma) {

                    CellInfoCdma cellInfoCdma = (CellInfoCdma) cellInfo;
                    CellIdentityCdma cellIdentity = cellInfoCdma.getCellIdentity();
                    CellSignalStrengthCdma cellSignalStrengthCdma = cellInfoCdma.getCellSignalStrength();
                    Log.d(LOG_TAG + "cell", "registered: " + cellInfoCdma.isRegistered());
                    Log.d(LOG_TAG + "cell", cellIdentity.toString());
                    Log.d(LOG_TAG + "cell", cellSignalStrengthCdma.toString());
                } else if (cellInfo instanceof CellInfoWcdma) {
                    CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) cellInfo;
                    CellIdentityWcdma cellIdentity = cellInfoWcdma.getCellIdentity();
                    CellSignalStrengthWcdma cellSignalStrengthWcdma = cellInfoWcdma.getCellSignalStrength();

                    Log.d(LOG_TAG + "cell", "registered: " + cellInfoWcdma.isRegistered());
                    Log.d(LOG_TAG + "cell", cellIdentity.toString());
                    Log.d(LOG_TAG + "cell", cellSignalStrengthWcdma.toString());

                } else if (cellInfo instanceof CellInfoLte) {
                    CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
                    CellIdentityLte cellIdentity = cellInfoLte.getCellIdentity();
                    CellSignalStrengthLte cellSignalStrengthLte = cellInfoLte.getCellSignalStrength();


                    Log.d(LOG_TAG + "cell", "registered: " + cellInfoLte.isRegistered());
                    Log.d(LOG_TAG + "cell", cellIdentity.toString());
                    Log.d(LOG_TAG + "cell", cellSignalStrengthLte.toString());


                }



            }


        }

    }
}