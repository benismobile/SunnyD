package org.fabeo.benbutchart.webmap;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.WakefulBroadcastReceiver;
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
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.Geofence.Builder ;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.fabeo.benbutchart.webmap.LocationUtils.getLatLngJSON;

/**
 * Created by benbutchart on 07/01/2015.
 */
public class LocationClient implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


    private GoogleApiClient apiClient ;
    private WebViewLocationAPI locationAPI ;
    private static final String LOG_TAG = "LocationClient" ;
    private boolean updatesPending = false ;
    private int updatesPendingInterval = 0 ;
    private boolean locationFixPending = false ;
    private Context appContext ;

    LocationFixListener fixListener ;
    LocationUpdateListener updateListener ;
    TrackUpdateReceiver trackUpdateReceiver ;
    GeofenceLivenessReceiver geofenceLivenessReceiver ;


    public LocationClient(Context appContext, WebViewLocationAPI locationAPI) {


        this.apiClient = new GoogleApiClient.Builder(appContext)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        this.appContext = appContext ;
        this.locationAPI = locationAPI ;
        this.fixListener = new LocationFixListener() ;
        this.updateListener = new LocationUpdateListener() ;
        this.trackUpdateReceiver = new TrackUpdateReceiver() ;
        this.geofenceLivenessReceiver = new GeofenceLivenessReceiver() ;
        this.apiClient.connect();


    }

    @Override
    public void onConnected(Bundle bundle) {



        // we've connected to GoogleApiClient so can now use FusedLocationAPI
        // initial location fix request
        Log.d(LOG_TAG, "onConnected") ;

        if(this.updatesPending)
        {
            this.requestLocationUpdates(this.updatesPendingInterval);
            this.updatesPending = false ;
            this.updatesPendingInterval = 0 ;
            Log.d(LOG_TAG ," pending location updates requested") ;
        }

        if(this.locationFixPending)
        {
           this.requestLocationFix();
           this.locationFixPending = false ;
           Log.d(LOG_TAG, " pending location fix requested") ;
        }

        // TODO allow user to control liveliness of geofences
        // TODO handle system power event and adjust geofence liveness interval
        // TODO only requestGeofenceLicenessUpdates after checking active geofences exist
        this.requestGeofenceLivenessUpdates(20000);



        WakefulBroadcastReceiver receiver = new WakefulAlarmBroadcastReceiver() ;
        appContext.registerReceiver(receiver, new IntentFilter("org.fabeo.benbutchart.webmap.ACTION_UPDATE_CELL_INFO"));
        Intent i = new Intent(appContext, WakefulAlarmBroadcastReceiver.class);
        i.setAction("org.fabeo.benbutchart.webmap.ACTION_UPDATE_CELL_INFO") ;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(appContext, 992, i, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager manager = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);
        long timeInterval = 60 * 1000 ;
        manager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), timeInterval, pendingIntent);
        Log.d(LOG_TAG, "Alarm set") ;

    }


    public void requestLocationFix()
    {

        if(apiClient.isConnected() == false)
        {
            this.locationFixPending = true ;
            Log.d(LOG_TAG, "postponing location fix until apiClient connected") ;
            return ;
        }


        // request a new location fix
        LocationRequest request = LocationRequest.create();
        request.setNumUpdates(1); // should already have initial location fix so one update enough
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(this.apiClient, request, this.fixListener);
        Log.d(LOG_TAG, "requested requestLocationFix") ;
    }


    public void requestLocationUpdates(int interval)
    {

           if(apiClient.isConnected() == false)
           {
               this.updatesPending = true ;
               this.updatesPendingInterval = interval ;
               Log.d(LOG_TAG, "postponing request location updates until apiClient connected") ;
               return ;
           }

           // request ongoing location updates
            LocationRequest request = LocationRequest.create();
            request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            request.setInterval(interval) ;
            LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, request, this.updateListener);
            Log.d(LOG_TAG, "requested location updates") ;
    }


    public void removeLocationUpdates()
    {
        LocationServices.FusedLocationApi.removeLocationUpdates(apiClient, this.updateListener) ;
    }

    public void addGeofence(int radius, Location location, String requestId)
    {

        Geofence.Builder builder = new Geofence.Builder();
        builder.setCircularRegion(location.getLatitude(), location.getLongitude(), radius )
               .setRequestId(requestId)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT)
                .setExpirationDuration(10 * 60 * 1000)
                .setLoiteringDelay(6000)
                .setNotificationResponsiveness(5000) ;

        Geofence geofence = builder.build() ;


        Intent geofenceTransitionIntent = new Intent() ;
        geofenceTransitionIntent.setAction(LocationUpdateIntentService.ACTION_GEOFENCE_TRANSITION) ;
        geofenceTransitionIntent.setClass(this.locationAPI.getApplicationContext(),
                org.fabeo.benbutchart.webmap.LocationUpdateIntentService.class) ;



        PendingIntent geofenceIntent = PendingIntent.getService(this.locationAPI.getApplicationContext(), 0,
                geofenceTransitionIntent, PendingIntent.FLAG_CANCEL_CURRENT);


        ArrayList<Geofence> geofences = new ArrayList<Geofence>(1) ;
        geofences.add(geofence) ;

        PendingResult<Status> result =
        LocationServices.GeofencingApi.addGeofences(apiClient, geofences, geofenceIntent) ;


        result.setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {


                if (status.isSuccess()) {

                    Log.d(LOG_TAG, "Geofence added") ;
                }

            }
        });

    }

    public void removeGeofence(int id)
    {
        // TODO remove geofence
    }

    public void requestGeofenceLivenessUpdates(int interval)
    {

        Log.d(LOG_TAG, "requestGeofenceLivenessUpdates") ;
        LocationRequest backgroundUpdateRequest = LocationRequest.create();
        backgroundUpdateRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        // ideally keep interval fairly large 20s plus to avoid excessive drain on battery
        backgroundUpdateRequest.setInterval(interval) ;

        Intent locationUpdateIntent = new Intent() ;
        locationUpdateIntent.setAction(LocationUpdateIntentService.ACTION_GEOFENCE_LIVENESS_UPDATE) ;
        locationUpdateIntent.setClass(this.locationAPI.getApplicationContext(),
                org.fabeo.benbutchart.webmap.LocationUpdateIntentService.class) ;

        PendingIntent locationIntent = PendingIntent.getService(this.locationAPI.getApplicationContext(), 0,
                locationUpdateIntent, PendingIntent.FLAG_CANCEL_CURRENT);


        PendingResult<Status> result =
                LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, backgroundUpdateRequest, locationIntent);

        result.setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {


                if (status.isSuccess()) {

                    Log.d(LOG_TAG, "Geofence liveness requested") ;
                }

            }
        });


        registerGeofenceLivenessReceiver();

    }



    public void stopGeofenceLivenessUpdates()
    {


        Intent locationUpdateIntent = new Intent() ;
        locationUpdateIntent.setAction(LocationUpdateIntentService.ACTION_GEOFENCE_LIVENESS_UPDATE) ;
        locationUpdateIntent.setClass(this.locationAPI.getApplicationContext(),
                org.fabeo.benbutchart.webmap.LocationUpdateIntentService.class) ;


        PendingIntent locationIntent = PendingIntent.getService(this.locationAPI.getApplicationContext(), 0,
                locationUpdateIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        PendingResult<Status> backgroundResult =
                LocationServices.FusedLocationApi.removeLocationUpdates(apiClient, locationIntent);
        locationIntent.cancel();

        backgroundResult.setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if(status.isSuccess()) {
                    Log.d(LOG_TAG, " removed tracking updates");
                }
            }
        }) ;

        unregisterGeofenceLivenessReceiver();

    }

    public void requestTrackUpdates(int interval, String trackid)
    {

        Log.d(LOG_TAG, "requestTrackUpdates") ;
        LocationRequest backgroundUpdateRequest = LocationRequest.create();
        backgroundUpdateRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        backgroundUpdateRequest.setInterval(interval) ;

        Intent locationUpdateIntent = new Intent() ;
        locationUpdateIntent.setAction(LocationUpdateIntentService.ACTION_LOCATION_UPDATE) ;
        locationUpdateIntent.setClass(this.locationAPI.getApplicationContext(),
                org.fabeo.benbutchart.webmap.LocationUpdateIntentService.class) ;
        locationUpdateIntent.putExtra("trackid" , trackid) ;

        PendingIntent locationIntent = PendingIntent.getService(this.locationAPI.getApplicationContext(), 0,
                locationUpdateIntent, PendingIntent.FLAG_CANCEL_CURRENT);


        PendingResult<Status> result =
                LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, backgroundUpdateRequest, locationIntent);

        result.setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {


                if (status.isSuccess()) {

                    Log.d(LOG_TAG, "location tracking requested") ;
                }

            }
        });


        registerTrackReceiver();

        //SWITCH off GeofenceLiveness updates - they are not necessary and may cause conflict with
        // high accuracy updates
        stopGeofenceLivenessUpdates();
    }


    public void stopTrackUpdates()
    {


          Intent locationUpdateIntent = new Intent() ;
          locationUpdateIntent.setAction(LocationUpdateIntentService.ACTION_LOCATION_UPDATE) ;
          locationUpdateIntent.setClass(this.locationAPI.getApplicationContext(),
                org.fabeo.benbutchart.webmap.LocationUpdateIntentService.class) ;


        PendingIntent locationIntent = PendingIntent.getService(this.locationAPI.getApplicationContext(), 0,
                    locationUpdateIntent, PendingIntent.FLAG_CANCEL_CURRENT);

          PendingResult<Status> backgroundResult =
                    LocationServices.FusedLocationApi.removeLocationUpdates(apiClient, locationIntent);
            locationIntent.cancel();

           backgroundResult.setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    if(status.isSuccess()) {
                        Log.d(LOG_TAG, " removed tracking updates");
                    }
                }
            }) ;

        unregisterTrackReceiver();

        // switch GEOFENCE Liveness updates back on if active
        // TODO add geofenceLive and update flags
        requestGeofenceLivenessUpdates(20000);
    }


    public void registerGeofenceLivenessReceiver()
    {
        IntentFilter filter = new IntentFilter();
        filter.addAction(LocationUpdateIntentService.ACTION_GEOFENCE_LIVENESS_UPDATE);

        if(this.geofenceLivenessReceiver!= null) {
            this.locationAPI.getApplicationContext().registerReceiver(this.geofenceLivenessReceiver, filter);
            Log.d(LOG_TAG, "registered TrackUpdateReceiver");
        }
        else
        {
            Log.e(LOG_TAG, " Could not register GeofenceLivenessReceiver") ;
        }

    }


    public void registerTrackReceiver()
    {
        IntentFilter filter = new IntentFilter();
        filter.addAction(LocationUtils.LOCATION_UPDATE_ACTION);

        if(this.trackUpdateReceiver!= null) {
            this.locationAPI.getApplicationContext().registerReceiver(this.trackUpdateReceiver, filter);
            Log.d(LOG_TAG, "registered TrackUpdateReceiver");
        }
        else
        {
            Log.e(LOG_TAG, " Could not register TrackUpdateReceiver") ;
        }

    }

    public void unregisterTrackReceiver()
    {
         if(this.trackUpdateReceiver!=null) {

             this.locationAPI.getApplicationContext().unregisterReceiver(this.trackUpdateReceiver);
             Log.d(LOG_TAG, "Unregistered TrackUpdateReceiver");
         }
        else
         {
             Log.e(LOG_TAG, " Could noe unregister TrackUpdateReceiver") ;
         }
    }


    public void unregisterGeofenceLivenessReceiver()
    {
        if(this.geofenceLivenessReceiver!=null) {

            this.locationAPI.getApplicationContext().unregisterReceiver(this.geofenceLivenessReceiver);
            Log.d(LOG_TAG, "Unregistered GeofenceLivenessReceiver");
        }
        else
        {
            Log.e(LOG_TAG, " Could noe unregister GeofenceReceiver") ;
        }
    }


    @Override
    public void onConnectionSuspended(int i) {

        Log.d(LOG_TAG, "onConnectionSuspended") ;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Log.d(LOG_TAG, "onConnectionFailed") ;
    }

    public class LocationUpdateListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            Log.d(LOG_TAG, "LocationUpdateListener:" + location) ;
            locationAPI.onLocationUpdate(location);

        }
    }




    public class LocationFixListener implements LocationListener
    {
        @Override
        public void onLocationChanged(Location location) {

         Log.d("LocationFixListener", "onLocationChanged" ) ;
         if(locationAPI != null) {
             Log.d("LocationFixListener", "set CurrentLocation" ) ;
             locationAPI.setCurrentLocation(location);
             Log.d("LocationFixListener", "onLocationFix" ) ;
             locationAPI.onLocationFix(location);
         }
        }
    }

    public class GeofenceLivenessReceiver extends  BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {


            Log.d(LOG_TAG, "GeofenceLivenessUpdateReceiver: onReceive called");
            Location updateLocation = (Location) intent.getExtras().get(LocationServices.FusedLocationApi.KEY_LOCATION_CHANGED);
            locationAPI.onGeofenceLivenessUpdate(updateLocation);
        }
    }

    public class TrackUpdateReceiver extends BroadcastReceiver
    {


        @Override
        public void onReceive(Context context, Intent intent) {


            Log.d(LOG_TAG, "TrackUpdateReceiver: onReceive called");
            Location updateLocation = (Location) intent.getExtras().get(LocationServices.FusedLocationApi.KEY_LOCATION_CHANGED);
            String latestTrackData = (String) intent.getExtras().get("org.fabeo.benbutchart.webmap.TRACKDATA");
            String latestPoint = (String) intent.getExtras().get("org.fabeo.benbutchart.webmap.POINT");

            Log.d(LOG_TAG, "onReceive: latestTrackData:" + latestTrackData );
            locationAPI.onTrackUpdate(latestTrackData);
        }

    }





}
