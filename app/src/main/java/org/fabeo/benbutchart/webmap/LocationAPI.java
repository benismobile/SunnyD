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
    private Location updateLocation ;
    private WebViewMap webViewMap ;
    private LocationFixListener fixListener = null ;
    private ForegroundLocationUpdateListener updateListener = null ;
    private BroadcastReceiver updateReceiver ;
    private PendingIntent locationIntent ;
    private Intent locationUpdateIntent = null ;

    public LocationAPI()
    {

    }

    public LocationAPI(WebViewMap webViewMap) {

        this.webViewMap = webViewMap ;
        this.apiClient = new GoogleApiClient.Builder(this.webViewMap)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        apiClient.connect();

        this.fixListener = new LocationFixListener() ;
        this.updateListener = new ForegroundLocationUpdateListener() ;

    }


    public Intent getLocationUpdateIntent()
    {

        return this.locationUpdateIntent ;
    }

    @Override
    public void onConnected(Bundle bundle) {

        // we've connected to GoogleApiClient so can now use FusedLocationAPI
        // initial location fix request
        Log.d("WebViewMap", "onConnected") ;
        LocationRequest request = LocationRequest.create();
        request.setNumUpdates(1);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, request, fixListener);

        // register receiver for background location updates
        this.updateReceiver = new BackgroundLocationUpdateReceiver();
        IntentFilter filter = new IntentFilter() ;
        filter.addAction("org.fabeo.benbutchart.webmap.LOCATION_UPDATE");
        this.webViewMap.registerReceiver(this.updateReceiver, filter);


    }

    @Override
    public void onConnectionSuspended(int i) {

        Log.d("WebViewMap" ,"ApiClient connection suspended" ) ;
        Toast.makeText(webViewMap, "ApiClient connection suspended", Toast.LENGTH_SHORT).show();

    }


    public void removeLocationUpdates()
    {
       // if(apiClient != null  ) {

            Log.d("WebViewMap", "removeLocationUpdates and unregister receiver") ;
            LocationServices.FusedLocationApi.removeLocationUpdates(apiClient, this.updateListener); // foreground updates
            LocationServices.FusedLocationApi.removeLocationUpdates(apiClient, this.locationIntent); // background updates
            this.webViewMap.unregisterReceiver(this.updateReceiver);
            this.updateReceiver = null ;


       // }

    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //TODO handle connection failed
    }


    @JavascriptInterface
    public void requestBackgroundLocationUpdates(int interval) {


        if(apiClient.isConnected())
        {
            LocationRequest request = LocationRequest.create();
            request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            request.setInterval(interval) ;

            this.locationUpdateIntent = new Intent() ;
            this.locationUpdateIntent.setAction("org.fabeo.benbutchart.webmap.LOCATION_UPDATE") ;
           // this.locationUpdateIntent.setClass(this.webViewMap, LocationAPI.BackgroundLocationUpdateReceiver.class) ;

            this.locationIntent = PendingIntent.getBroadcast(this.webViewMap, 0,
                            locationUpdateIntent, PendingIntent.FLAG_CANCEL_CURRENT);


            PendingResult<Status> result = LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, request, this.locationIntent);

           result.setResultCallback(new ResultCallback<Status>() {
               @Override
               public void onResult(Status status) {
                   Log.d("WebViewMap", " status success: " + status.isSuccess() + " status code:" + status.getStatusCode());

               }
           });


        }

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

              // TODO apiClient.connect();
              // TODO updatesRequestPending = true ;
              //TODO handle apiClinet not connected
              // should we call javascript callback with null JSON string  or just not call?
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
        request.setNumUpdates(1); // should already have location fix so one update enough
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

            updateLocation = location;

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


            Log.d("WebViewMap", "Background Location Update Recevier onRecieve called");
            // Toast.makeText(webViewMap, "LocationUpdateReceiver:onRecieve(): " , Toast.LENGTH_SHORT).show();
            final Location updateLocation = (Location) intent.getExtras().get(LocationServices.FusedLocationApi.KEY_LOCATION_CHANGED);

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
