package org.fabeo.benbutchart.webmap;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
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


    LocationFixListener fixListener ;
    LocationUpdateListener updateListener ;
    TrackUpdateListener trackUpdateListener ;

    public LocationClient(Context appContext, WebViewLocationAPI locationAPI) {


        this.apiClient = new GoogleApiClient.Builder(appContext)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        this.locationAPI = locationAPI ;
        this.apiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {

        this.fixListener = new LocationFixListener() ;
        this.updateListener = new LocationUpdateListener() ;
        this.trackUpdateListener = new TrackUpdateListener() ;


        // we've connected to GoogleApiClient so can now use FusedLocationAPI
        // initial location fix request
        Log.d(LOG_TAG, "onConnected") ;
        LocationRequest locationFixRequest = LocationRequest.create();
        // run update a couple of times in hope we might get more accurate result as first location often approximated
        locationFixRequest.setInterval(2000) ;
        locationFixRequest.setNumUpdates(3);
        locationFixRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(this.apiClient, locationFixRequest, this.fixListener);
        this.locationInitialized = true ;

        if(this.updatesPending)
        {
            this.requestLocationUpdates(this.updatesPendingInterval);
            this.updatesPending = false ;
            this.updatesPendingInterval = 0 ;
            Log.d(LOG_TAG ," pending location updates requested") ;
        }
    }


    public void requestLocationFix()
    {

        // request a new location fix
        LocationRequest request = LocationRequest.create();
        request.setNumUpdates(1); // should already have initial location fix so one update enough
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(this.apiClient, request, this.fixListener);
        Log.d(LOG_TAG, "requested requestLocationFix") ;
    }


    public void requestLocationUpdates(int interval)
    {
        // request ongoing location updates
           if(apiClient.isConnected() == false)
           {
               this.updatesPending = true ;
               this.updatesPendingInterval = interval ;
               Log.d(LOG_TAG, "postponing request location updates until apiClient connected") ;
               return ;
           }

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

    public class TrackUpdateListener implements LocationListener
    {

        @Override
        public void onLocationChanged(Location location) {
            // TODO read latest GPX file
            // TODO locationAPI.onLocationUpdate(gpxString);
            throw new UnsupportedOperationException() ;
         }
    }



}
