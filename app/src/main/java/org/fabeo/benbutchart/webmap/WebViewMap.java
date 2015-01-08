package org.fabeo.benbutchart.webmap;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;


public class WebViewMap extends Activity {

    WebView webView = null ;
    WebViewLocationAPI locationAPI = null ;
    static SharedPreferences location_api_state_prefs ;
    static SharedPreferences.Editor location_api_state_editor ;
    private static final String LOG_TAG = "WebViewMap Activity" ;

    private static final String UPDATES_REQUESTED_STATUS_KEY = "UPDATES_REQUESTED" ;
    private static final String UPDATE_INTERVAL_KEY = "UPDATE_INTERVAL" ;

    private boolean isUpdatesRequested = false ;
    private int updateInterval = 0 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Log.d(LOG_TAG, "onCreate()") ;

        if(savedInstanceState != null)
        {

            Log.d(LOG_TAG, "onCreate saved instance state: Updates:"
                    + savedInstanceState.getBoolean(UPDATES_REQUESTED_STATUS_KEY)
                    + " Interval:" + savedInstanceState.getInt(UPDATE_INTERVAL_KEY)
            ) ;
            this.updateInterval = savedInstanceState.getInt(UPDATE_INTERVAL_KEY) ;
            this.isUpdatesRequested =    savedInstanceState.getBoolean(UPDATES_REQUESTED_STATUS_KEY) ;
        }


        // Open Shared Preferences
        location_api_state_prefs = getSharedPreferences("LOCATION_API_STATE", Context.MODE_PRIVATE);
        // Get an editor
        location_api_state_editor = location_api_state_prefs.edit();


        this.webView = new WebView(this) ;

        this.locationAPI = new WebViewLocationAPI(this.webView);

        setContentView(this.webView);


        WebSettings webSettings = this.webView.getSettings() ;
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setBlockNetworkImage(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadsImagesAutomatically(true);

        this.webView.addJavascriptInterface(this.locationAPI, "AndroidLocationAPI");

        this.webView.post(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("file:///android_asset/html/map.html");
            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "onStart()") ;


        if(this.webView == null) {
            Log.d(LOG_TAG, "onStart: webView instance is null") ;

        }
        // create new LocationAPI instance - will create LocationClient which in turn will request connection to GoogleAPI client
        if(this.locationAPI == null) {
            Log.d(LOG_TAG, "onStart: locationAPI instance null") ;
            this.locationAPI = new WebViewLocationAPI(this.webView);
        }



    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(LOG_TAG, "onRestart()") ;

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume()") ;

        if(this.webView == null) {
            Log.d(LOG_TAG, "onResume(): webView instance is null") ;

        }

        if(this.locationAPI == null )
        {
            Log.e(LOG_TAG, "onResume(): locationAPI instance is null") ;
            this.locationAPI = new WebViewLocationAPI(this.webView) ;
        }

        Log.d(LOG_TAG, "onResume(): isUpdatesRequested" + this.isUpdatesRequested ) ;

        if(this.isUpdatesRequested)
        {
            Log.d(LOG_TAG, "onResume(): resume upddates with interval" + this.updateInterval ) ;
            this.locationAPI.requestLocationUpdates(this.updateInterval);
        }

/*
        Log.d(LOG_TAG, "onResume(): wereUpdatesRequested:" + wereUpdatesRequested ) ;
        if(wereUpdatesRequested)
        {
            // restart location requests
//            int updateInterval = location_api_state_prefs.getInt(UPDATE_INTERVAL_KEY, 0) ;
            Log.d(LOG_TAG, "onResume():updateinterval was: " + updateInterval ) ;
            this.locationAPI.requestLocationUpdates(updateInterval);
        }
*/
    }


    protected void onPause(){
        super.onPause();
        Log.d(LOG_TAG, "onPause") ;

    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.d(LOG_TAG, "onStop") ;

        Log.d(LOG_TAG, "onStop(): is isUpdatesRequested:" + locationAPI.isUpdatesRequested() + " updateInterval: " + locationAPI.getUpdateInterval()) ;

    //    location_api_state_editor.putBoolean(UPDATES_REQUESTED_STATUS_KEY, locationAPI.isUpdatesRequested());
    //    location_api_state_editor.commit();
    //    location_api_state_editor.putInt(UPDATE_INTERVAL_KEY, locationAPI.getUpdateInterval()) ;

        this.isUpdatesRequested = this.locationAPI.isUpdatesRequested() ;
        this.updateInterval = this.locationAPI.getUpdateInterval() ;

        // activity no longer visible so stop foreground updates
        if(this.isUpdatesRequested) {
            this.locationAPI.stopLocationUpdates();
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(LOG_TAG, "onSavedInstanceState()") ;
        outState.putInt(UPDATE_INTERVAL_KEY, locationAPI.getUpdateInterval());
        outState.putBoolean(UPDATES_REQUESTED_STATUS_KEY, locationAPI.isUpdatesRequested());

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        Log.d(LOG_TAG, "onRestoreInstanceState()") ;


        Log.d(LOG_TAG, "onRestoreInstanceStateinstance state: Updates:"
                        + savedInstanceState.getBoolean(UPDATES_REQUESTED_STATUS_KEY)
                        + " Interval:" + savedInstanceState.getInt(UPDATE_INTERVAL_KEY)
        ) ;
        this.updateInterval = savedInstanceState.getInt(UPDATE_INTERVAL_KEY) ;
        this.isUpdatesRequested =    savedInstanceState.getBoolean(UPDATES_REQUESTED_STATUS_KEY) ;

    }


    @Override
    protected void onDestroy() {

        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy()") ;
//        location_api_state_editor.clear() ;
  //      location_api_state_editor.commit() ;

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_web_view_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        else if(id == R.id.action_get_location)
        {

            webView.post(new Runnable(){
                @Override
                public void run()
                {
                    webView.loadUrl("javascript:getCurrentLocation();");
                }

            });

            return true ;
        }
        else if(id == R.id.action_request_location_updates)
        {
            webView.post(new Runnable(){
               @Override
               public void run()
               {
                   webView.loadUrl("javascript:requestLocationUpdates();");
               }

            });
            return true ;
        }
        else if(id == R.id.action_request_background_location_updates)
        {
            webView.post(new Runnable(){
                @Override
                public void run()
                {
                    webView.loadUrl("javascript:requestBackgroundUpdates();");
                }

            });
            return true ;
        }



        return super.onOptionsItemSelected(item);
    }


    public WebView getWebView()
    {

        return this.webView ;
    }
}
