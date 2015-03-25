package org.fabeo.benbutchart.webmap;

import android.content.Context;
import android.location.Location;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;


import static org.fabeo.benbutchart.webmap.LocationUtils.getLatLngJSON;

/**
 * Created by benbutchart on 07/01/2015.
 */
public class WebViewLocationAPI
{

    private WebView webView ;
    private Location currentLocation = null ;
    private LocationClient locationClient ;
    private static final String LOG_TAG = "WebViewLocationAPI" ;
    private boolean updatesRequested = false ;
    private boolean trackingRequested = false ;
    private boolean locationFixObtained = false ;
    private int updateInterval = 0 ;
    private int trackingInterval = 0 ;
    private boolean isCallbackScriptLoaded = false ;

    public WebViewLocationAPI(WebView webView)
    {
        this.webView = webView ;
        this.locationClient = new LocationClient(webView.getContext() , this) ;
    }


    public WebView getWebView()
    {
        return this.webView ;
    }

    public Context getApplicationContext()
    {

        return this.webView.getContext() ;
    }

   @JavascriptInterface
   public boolean isLocationFixObtained()
   {
       return this.locationFixObtained ;
   }


    public void onLocationUpdate(Location location)
    {
        final Location updateLocation = location ;
        this.currentLocation = location ;
        this.locationFixObtained = true ;

        if(webView != null ) {
            webView.post(new Runnable() {
                @Override
                public void run() {
                    String latlon = getLatLngJSON(updateLocation);
                    webView.loadUrl("javascript:onLocationUpdate('" + latlon + "');");

                }
            });

        }
    }




    public void onLocationFix(Location location)
    {
        final Location locationFix = location ;
        this.currentLocation = location ;
        this.locationFixObtained = true ;

        if(this.webView != null ) {
            this.webView.post(new Runnable() {
                @Override
                public void run() {

                    final String latlon = getLatLngJSON(locationFix);
                    Log.d(LOG_TAG, "callback to onLocationFix() with latlon" + latlon) ;
                        webView.loadUrl("javascript:onLocationFix('" + latlon + "');");

                }
            });
        }
    }


    public void onTrackUpdate(String GPXTrackData)
    {
        final String gpxTrackData = GPXTrackData ;

        if(webView != null ) {
            webView.post(new Runnable() {
                @Override
                public void run() {

                    webView.loadUrl("javascript:onTrackUpdate('" + gpxTrackData + "');");

                }
            });

        }
    }

    public void onShowTrack(String GPXTrackData)
    {
        final String gpxTrackData = GPXTrackData ;

        if(webView != null ) {
            webView.post(new Runnable() {
                @Override
                public void run() {

                    webView.loadUrl("javascript:onShowTrack('" + gpxTrackData + "');");

                }
            });

        }
    }



    public void onDeleteTrack(String trackId)
    {
        final String trkid = trackId ;

        TrackingDataSQLiteOpenHelper dbHelper = new TrackingDataSQLiteOpenHelper(this.getApplicationContext()) ;
        dbHelper.deleteTrack(trackId);

        if(webView != null ) {
            webView.post(new Runnable() {
                @Override
                public void run() {
                    webView.loadUrl("javascript:onTrackDelete('" + trkid + "');");
                }
            });
        }
    }


    public void createGeofence(String id)
    {
        locationClient.addGeofence(30, this.currentLocation, id);

    }

    //TODO  Javascript interface version of this method with JSON String argument
    public void setCurrentLocation(Location location)
    {
        this.currentLocation = location ;
        this.locationFixObtained = true ;
    }


    // not part of Javascript interface
    public Location getInternalLocation()
    {
       return this.currentLocation ;
    }

     @JavascriptInterface
    public void requestLocationFix()
    {
       this.locationClient.requestLocationFix();

    }


    @JavascriptInterface
    public void requestLocationUpdates(int interval)
    {

        Log.d(LOG_TAG, "StopLocationUpdates: " + interval) ;
        this.locationClient.requestLocationUpdates(interval);
        this.updatesRequested = true ;
        this.updateInterval = interval ;
    }


    @JavascriptInterface
    public void stopLocationUpdates()
    {
        Log.d(LOG_TAG, "Stop Location Updates") ;
        locationClient.removeLocationUpdates();
        this.updatesRequested = false ;

    }

    @JavascriptInterface
    public void startTrackUpdates(int interval, String trackid)
    {
        Log.d(LOG_TAG, " request track Updates") ;
        locationClient.requestTrackUpdates(interval, trackid);
        this.trackingRequested = true ;
        this.trackingInterval = interval ;
    }

    @JavascriptInterface
    public void stopTrackUpdates()
    {
        Log.d(LOG_TAG, " stop track Updates") ;
        locationClient.stopTrackUpdates();
        this.trackingRequested = false ;
        this.trackingInterval = 0 ;
    }

    public void registerTrackReceiver()
    {
        this.locationClient.registerTrackReceiver();
    }

    public void unRegisterTrackReceiver()
    {
        this.locationClient.unregisterTrackReceiver();
    }

    @JavascriptInterface
    public boolean isUpdatesRequested()
    {
        return this.updatesRequested ;
    }


    @JavascriptInterface
    public int getUpdateInterval()
    {
        return this.updateInterval ;
    }

    @JavascriptInterface
    public boolean isTracking()
    {
        return this.trackingRequested ;
    }

    @JavascriptInterface
    public int getTrackingInterval()
    {
        return this.trackingInterval ;
    }


    public boolean isCallbackScriptLoaded() {
        return isCallbackScriptLoaded;
    }

    public void setCallbackScriptLoaded(boolean isCallbackScriptLoaded) {
        this.isCallbackScriptLoaded = isCallbackScriptLoaded;
    }

    public void onGeofenceLivenessUpdate(Location updateLocation) {

        // generally the liveness update is only intended to keep geofences active
        // but may as well use it to update current location
        this.currentLocation = updateLocation ;
    }
}
