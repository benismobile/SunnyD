package org.fabeo.benbutchart.webmap;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

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

    LocationFixListener fixListener ;
    LocationUpdateListener updateListener ;
    TrackUpdateReceiver trackUpdateReceiver ;

    public LocationClient(Context appContext, WebViewLocationAPI locationAPI) {


        this.apiClient = new GoogleApiClient.Builder(appContext)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        this.locationAPI = locationAPI ;
        this.fixListener = new LocationFixListener() ;
        this.updateListener = new LocationUpdateListener() ;
        this.trackUpdateReceiver = new TrackUpdateReceiver() ;

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

    public class TrackUpdateReceiver extends BroadcastReceiver
    {


        @Override
        public void onReceive(Context context, Intent intent) {


            Log.d(LOG_TAG, "onReceive called");
            Location updateLocation = (Location) intent.getExtras().get(LocationServices.FusedLocationApi.KEY_LOCATION_CHANGED);
            String latestTrackData = (String) intent.getExtras().get("org.fabeo.benbutchart.webmap.TRACKDATA");
            String latestPoint = (String) intent.getExtras().get("org.fabeo.benbutchart.webmap.POINT");

            Log.d(LOG_TAG, "onReceive: latestTrackData:" + latestTrackData );
            locationAPI.onTrackUpdate(latestTrackData);
        }

    }





}
