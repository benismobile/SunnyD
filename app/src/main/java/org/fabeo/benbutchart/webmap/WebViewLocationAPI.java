package org.fabeo.benbutchart.webmap;

import android.location.Location;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

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
    private boolean locationFixObtained = false ;
    private int updateInterval = 0 ;
    private boolean isCallbackScriptLoaded = false ;

    public WebViewLocationAPI(WebView webView)
    {
        this.webView = webView ;
        this.locationClient = new LocationClient(webView.getContext() , this) ;
    }



   @JavascriptInterface
   public boolean isLocationFixObtained()
   {
       return this.locationFixObtained ;
   }

   @JavascriptInterface
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

    @JavascriptInterface
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
                    if(isCallbackScriptLoaded) {
                        webView.loadUrl("javascript:onLocationFix('" + latlon + "');");
                    }
                    else
                    {
                        Log.d(LOG_TAG, "could not execute callback - javascript still loading will request another location fix" ) ;
                        requestLocationFix();
                    }

                }
            });

        }
    }


    @JavascriptInterface
    public void onTrackUpdate(String GPXTrackData)
    {
        final String gpxTrackData = GPXTrackData ;

        if(webView != null ) {
            webView.post(new Runnable() {
                @Override
                public void run() {
                    // String latlon = getLatLngJSON(updateLocation);
                    // TODO obtain GPX String
                    webView.loadUrl("javascript:onTrackUpdate('" + gpxTrackData + "');");

                }
            });

        }
    }

    //TODO  Javascript interface version of this method with JSON String argument
    public void setCurrentLocation(Location location)
    {
        this.currentLocation = location ;
        this.locationFixObtained = true ;
    }

    @JavascriptInterface
    public String getCurrentLocation()
    {
        if(this.currentLocation==null) return "[}" ;

        String latlon = getLatLngJSON(this.currentLocation);
        return latlon;
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

        this.locationClient.requestLocationUpdates(interval);
        this.updatesRequested = true ;
        this.updateInterval = interval ;
    }

    @JavascriptInterface
    public void stopLocationUpdates()
    {
        locationClient.removeLocationUpdates();
        this.updatesRequested = false ;

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

    public boolean isCallbackScriptLoaded() {
        return isCallbackScriptLoaded;
    }

    public void setCallbackScriptLoaded(boolean isCallbackScriptLoaded) {
        this.isCallbackScriptLoaded = isCallbackScriptLoaded;
    }
}
