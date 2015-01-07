package org.fabeo.benbutchart.webmap;

import android.location.Location;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
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
    private Location currentLocation ;

    public WebViewLocationAPI(WebView webView)
    {
        this.webView = webView ;
    }

   @JavascriptInterface
    public void onLocationUpdate(Location location)
    {
        final Location updateLocation = location ;
        this.currentLocation = location ; // TODO Should this be updated here or not?

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
    public void onTrackUpdate(final String GPXTrackData)
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

    @JavascriptInterface
    public void setCurrentLocation(Location location)
    {
        this.currentLocation = location ;
    }

    @JavascriptInterface
    public String getCurrentLocation()
    {
        if(this.currentLocation==null) return "[}" ;

        String latlon = getLatLngJSON(this.currentLocation);
        return latlon;
    }





}
