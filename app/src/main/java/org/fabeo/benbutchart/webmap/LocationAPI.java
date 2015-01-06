package org.fabeo.benbutchart.webmap;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;

import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import static android.util.Log.*;
import static org.fabeo.benbutchart.webmap.LocationUtils.getLatLngJSON;

/**
 * Created by benbutchart on 06/12/2014.
 * Class intended to provide interface to FusedLocationAPI functions from a WebView Javascript interface
 */
public class LocationAPI implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient apiClient ;
    private Location currentLocation ;
 //   private Location updateLocation ;
    private WebViewMap webViewMap ;
    private LocationFixListener fixListener = null ;
    private ForegroundLocationUpdateListener updateListener = null ;
    private BroadcastReceiver updateReceiver ;
    private PendingIntent locationIntent ;
    private Intent locationUpdateIntent = null ;
    private int currentUpdateStatus = UPDATES_NOT_INITIALISED ;

    private boolean backgroundUpdatesRequested = true ;
    private int backgroundUpdateInterval = 2000 ;



    public static final int UPDATES_NOT_INITIALISED = 0 ;
    public static final int UPDATES_BROADCASTING = 1 ;
    public static final int UPDATES_RECEIVER_REGISTERED = 2 ;
    public static final int UPDATES_MARKED_REMOVE = 3 ;

    public static final String KEY_CURRENT_UPDATE_STATUS = "LOCATION_API_UPDATE_STATUS";

    public LocationAPI(WebViewMap webViewMap) {

        this.webViewMap = webViewMap ;

        this.apiClient = new GoogleApiClient.Builder(this.webViewMap)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        apiClient.connect();

    }


    public GoogleApiClient getApiClient()
    {
        return this.apiClient ;

    }

    public int getCurrentUpdateStatus()
    {
        return this.currentUpdateStatus ;
    }

    public PendingIntent getLocationUpdateIntent()
    {

        return this.locationIntent ;
    }


    public BroadcastReceiver getUpdateReceiver()
    {

        return this.updateReceiver ;
    }

    @Override
    public void onConnected(Bundle bundle) {

        this.fixListener = new LocationFixListener() ;
        this.updateListener = new ForegroundLocationUpdateListener() ;


        // we've connected to GoogleApiClient so can now use FusedLocationAPI
        // initial location fix request
        Log.d("LocationAPI", "onConnected") ;
        LocationRequest locationFixRequest = LocationRequest.create();
        locationFixRequest.setNumUpdates(3); // get initial location fix - run a few times to get more accurate result
        locationFixRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, locationFixRequest, fixListener); // fixlistener sets current location member variable



        // TODO if the user has requested background location updates we will now request them

        LocationRequest backgroundUpdateRequest = LocationRequest.create();
        backgroundUpdateRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        backgroundUpdateRequest.setInterval(this.backgroundUpdateInterval) ;

        this.locationUpdateIntent = new Intent() ;
        this.locationUpdateIntent.setAction("org.fabeo.benbutchart.webmap.LOCATION_UPDATE") ;
        // this.locationUpdateIntent.setClass(this.webViewMap, LocationAPI.BackgroundLocationUpdateReceiver.class) ;

        Log.d("LocationAPI", "requestBackgroundLocationUpdates currentUpdateStatus" + currentUpdateStatus) ;


        this.locationIntent = PendingIntent.getBroadcast(this.webViewMap, 0,
                locationUpdateIntent, PendingIntent.FLAG_CANCEL_CURRENT);


        PendingResult<Status> result =
                LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, backgroundUpdateRequest, this.locationIntent);

        result.setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                Log.d("LocationAPI", " status success: " + status.isSuccess() + " status code:" + status.getStatusCode());

                if (status.isSuccess()) {

                    currentUpdateStatus = UPDATES_BROADCASTING;
                }

            }
        });




    }

    @Override
    public void onConnectionSuspended(int i) {

        Log.d("LocationAPI" ,"ApiClient connection suspended" ) ;
        Toast.makeText(webViewMap, "ApiClient connection suspended", Toast.LENGTH_SHORT).show();

    }



    public void removeBackgroundLocationUpdates()
    {
        //Log.d("LocationAPI", " remove current locationUpdates and unregister receiver: apiClient: " + apiClient + " updateListener: " + this.updateListener + " updateReceiver: " + this.updateReceiver + " pendingIntent:" + this.locationIntent ) ;
        //Log.d("LocationAPI", "remove background location updates");



        if(locationIntent==null)
        {

            this.locationUpdateIntent = new Intent() ;
            this.locationUpdateIntent.setAction("org.fabeo.benbutchart.webmap.LOCATION_UPDATE") ;

            this.locationIntent = PendingIntent.getBroadcast(this.webViewMap, 0,
                    locationUpdateIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        }

        PendingResult<Status> backgroundResult = LocationServices.FusedLocationApi.removeLocationUpdates(this.apiClient, this.locationIntent);
        this.locationIntent.cancel();

        backgroundResult.setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                Log.d("LocationAPI", " background update removed status success: " + status.isSuccess() + " status code:" + status.getStatusCode());

            }
        }) ;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //TODO handle connection failed

        Log.d("Location API" , "onConnectionFailed") ;
    }

    public void registerUpdateReceiver()
    {

        this.updateReceiver = new BackgroundLocationUpdateReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("org.fabeo.benbutchart.webmap.LOCATION_UPDATE");
        this.webViewMap.registerReceiver(this.updateReceiver, filter);
        this.currentUpdateStatus = UPDATES_RECEIVER_REGISTERED;

    }

    public static void unRegisterUpdateReceiver(Context context)
    {
        BackgroundLocationUpdateReceiver receiver = new BackgroundLocationUpdateReceiver() ;
        context.unregisterReceiver();

    }


    public void unregisterUpdateReceiver()
    {

        if(this.updateReceiver == null) {
            this.updateReceiver = new BackgroundLocationUpdateReceiver();

        }

        this.webViewMap.unregisterReceiver(this.updateReceiver);
    }

    @JavascriptInterface
    public void requestBackgroundLocationUpdates(int interval) {

        Log.d("LocationAPI", "user requested background location updates") ;

    }







        @JavascriptInterface
    public void requestLocationUpdates(int interval)
    {
          if(apiClient.isConnected())
          {
              LocationRequest request = LocationRequest.create();
              request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
              request.setInterval(interval) ;
              LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, request, this.updateListener);
              Toast.makeText(webViewMap, "Requested Location Updates", Toast.LENGTH_SHORT).show();

          }
          else
          {
              Toast.makeText(webViewMap, "Request Location Updates apiClient not connected", Toast.LENGTH_SHORT).show();
              //TODO handle apiClinet not connected
          }
    }


    @JavascriptInterface
    public String getLocation() {

        if(!apiClient.isConnected())
        {
            Toast.makeText(webViewMap, "Problem getting location fix: Google services not connected", Toast.LENGTH_SHORT).show();
            return "{}";
        }

        LocationRequest request = LocationRequest.create();
        request.setNumUpdates(1); // should already have initial location fix so one update enough
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, request, this.fixListener);


        if (this.currentLocation == null) // must be a problem getting a location fix
        {
          Toast.makeText(webViewMap, "Problem getting location fix: currentLocation null", Toast.LENGTH_SHORT).show();

        }
        else
        {
           String latlon = getLatLngJSON(webViewMap, this.currentLocation);
           return latlon;
        }
        return "{}";
    }

    public void setCurrentUpdateStatus(int status) {

        this.currentUpdateStatus = status ;
    }


    public class LocationFixListener implements LocationListener
    {
        @Override
        public void onLocationChanged(Location location) {
            currentLocation = location;
        }

    }

    public class ForegroundLocationUpdateListener implements LocationListener
    {

        @Override
        public void onLocationChanged(Location location) {

            final Location updateLocation = location; // TODO does updateLocation need to be a member variable?
            currentLocation = location ;

            if(webViewMap != null && webViewMap.getWebView() != null ) {
                webViewMap.getWebView().post(new Runnable() {
                    @Override
                    public void run() {
                        String latlon = getLatLngJSON(webViewMap, updateLocation);
                        webViewMap.getWebView().loadUrl("javascript:onLocationUpdate('" + latlon + "');");

                    }
                });

            }
        }

    }

    // This receiver has to be static so that the receiver can be declared in the Android manifest file
    public class BackgroundLocationUpdateReceiver extends BroadcastReceiver {


        @Override
        public void onReceive(Context context, Intent intent) {


            Log.d("LocationAPI", "Background Location Update Recevier onRecieve called");
            // Toast.makeText(webViewMap, "LocationUpdateReceiver:onRecieve(): " , Toast.LENGTH_SHORT).show();
            final Location updateLocation = (Location) intent.getExtras().get(LocationServices.FusedLocationApi.KEY_LOCATION_CHANGED);

            currentLocation = updateLocation ;

            if (webViewMap != null && webViewMap.getWebView() != null) {
                webViewMap.getWebView().post(new Runnable() {
                    @Override
                    public void run() {
                        String latlon = getLatLngJSON(webViewMap, updateLocation);
                        webViewMap.getWebView().loadUrl("javascript:onLocationUpdate('" + latlon + "');");

                    }
                });


            }
        }
    }
}
