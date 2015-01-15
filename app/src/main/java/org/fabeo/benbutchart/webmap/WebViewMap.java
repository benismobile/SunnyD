package org.fabeo.benbutchart.webmap;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;


public class WebViewMap extends Activity {

    WebView webView = null ;
    WebViewLocationAPI locationAPI = null ;

    static SharedPreferences location_api_state_prefs ;

    static SharedPreferences.Editor location_api_state_editor ;

    private static final String LOG_TAG = "WebViewMap Activity" ;
    private static final String UPDATES_REQUESTED_STATUS_KEY = "UPDATES_REQUESTED" ;
    private static final String UPDATE_INTERVAL_KEY = "UPDATE_INTERVAL" ;
    private static final String CURRENT_LOCATION_KEY =  "CURRENT_LOCATION";
    private static final String IS_LOCATION_FIX_KEY = "IS_LOCATION_FIX" ;
    private static final String TRACKING_REQUESTED_STATUS_KEY = "TRACKING_REQUESTED" ;
    private static final String TRACKING_INTERVAL_KEY = "TRACKING_KEY" ;


    private boolean isTrackingRequested = false ;
    private int trackingInterval = 0 ;
    private boolean isUpdatesRequested = false ;
    private int updateInterval = 0 ;
    private Location currentLocation ;
    private boolean isLocationFixObtained = false ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Log.d(LOG_TAG, "onCreate()") ;

        location_api_state_prefs = getSharedPreferences("LOCATION_API_PREFS", Context.MODE_PRIVATE);
        ;location_api_state_editor = location_api_state_prefs.edit() ;

        if(savedInstanceState != null)
        {

            Log.d(LOG_TAG, "onCreate saved instance state: Updates:"
                    + savedInstanceState.getBoolean(UPDATES_REQUESTED_STATUS_KEY)
                    + " Interval:" + savedInstanceState.getInt(UPDATE_INTERVAL_KEY)
                    + " isLocationFixObtained:" + savedInstanceState.getBoolean(IS_LOCATION_FIX_KEY)
                    + " currentLocation:" + savedInstanceState.getParcelable(CURRENT_LOCATION_KEY)
                    + " tracking:" + savedInstanceState.getBoolean(TRACKING_REQUESTED_STATUS_KEY)
                    + " tracking interval:" + savedInstanceState.getInt(TRACKING_INTERVAL_KEY)
            ) ;
            this.updateInterval = savedInstanceState.getInt(UPDATE_INTERVAL_KEY) ;
            this.isUpdatesRequested =    savedInstanceState.getBoolean(UPDATES_REQUESTED_STATUS_KEY) ;
            this.currentLocation = savedInstanceState.getParcelable(CURRENT_LOCATION_KEY) ;
            this.isLocationFixObtained = savedInstanceState.getBoolean(IS_LOCATION_FIX_KEY) ;
            //TODO use preferences instead of instance state for tracking status and tracking can outlive the process
            //TODO we mught need to keep these instance variables for track listener though?
        //    this.isTrackingRequested = savedInstanceState.getBoolean(TRACKING_REQUESTED_STATUS_KEY) ;
        //    this.trackingInterval = savedInstanceState.getInt(TRACKING_INTERVAL_KEY) ;

        }
        // tracking outlives activity so cannot rely on
        this.isTrackingRequested = location_api_state_prefs.getBoolean(TRACKING_REQUESTED_STATUS_KEY, false) ;

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



        this.webView.setWebViewClient(new WebViewClient() {
            @Override

            public void onPageFinished(WebView view, String url) {

                if("file:///android_asset/html/map.html".equals(url))
                {
                    Log.d(LOG_TAG, " LOADED map.html") ;
                    locationAPI.setCallbackScriptLoaded(true);
                    // TODO check location fix pending - request locationfix - get rid of messy retry code
                }
                else
                {
                    Log.d(LOG_TAG, "onPageFinished:" + url ) ;
                }

            }
        });


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

        // TODO do we need this check?
        if(this.webView == null) {
            Log.d(LOG_TAG, "onResume(): webView instance is null") ;

        }
        // TODO do we need this check?
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


        this.isUpdatesRequested = this.locationAPI.isUpdatesRequested() ;
        this.updateInterval = this.locationAPI.getUpdateInterval() ;

        // activity no longer visible so stop foreground updates
        if(this.isUpdatesRequested) {
            this.locationAPI.stopLocationUpdates();
        }

        // have to save tracking status to preferences as tracking will last beyond activity life cycle
        location_api_state_editor.putBoolean(TRACKING_REQUESTED_STATUS_KEY,this.isTrackingRequested) ;
        location_api_state_editor.commit() ;

    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(LOG_TAG, "onSavedInstanceState()") ;
        outState.putInt(UPDATE_INTERVAL_KEY, locationAPI.getUpdateInterval());
        outState.putBoolean(UPDATES_REQUESTED_STATUS_KEY, locationAPI.isUpdatesRequested());

        outState.putBoolean(TRACKING_REQUESTED_STATUS_KEY, locationAPI.isTracking());
        outState.putInt(TRACKING_INTERVAL_KEY, locationAPI.getTrackingInterval());
        outState.putParcelable(CURRENT_LOCATION_KEY, locationAPI.getInternalLocation());
        outState.putBoolean(IS_LOCATION_FIX_KEY, locationAPI.isLocationFixObtained());

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        Log.d(LOG_TAG, "onRestoreInstanceState()") ;


        Log.d(LOG_TAG, "onRestoreInstanceStateinstance state: Updates:"
                        + savedInstanceState.getBoolean(UPDATES_REQUESTED_STATUS_KEY)
                        + " Interval:" + savedInstanceState.getInt(UPDATE_INTERVAL_KEY)
                        + " isLocationFixObtained:" + savedInstanceState.getBoolean(IS_LOCATION_FIX_KEY)
                        + " currentLocation:" + savedInstanceState.getParcelable(CURRENT_LOCATION_KEY)
                        + " tracking:" + savedInstanceState.getBoolean(TRACKING_REQUESTED_STATUS_KEY)
                        + " tracking interval:" + savedInstanceState.getInt(TRACKING_INTERVAL_KEY)
        ) ;

        this.updateInterval = savedInstanceState.getInt(UPDATE_INTERVAL_KEY) ;
        this.isUpdatesRequested =    savedInstanceState.getBoolean(UPDATES_REQUESTED_STATUS_KEY) ;

        this.currentLocation = savedInstanceState.getParcelable(CURRENT_LOCATION_KEY) ;
        this.isLocationFixObtained = savedInstanceState.getBoolean(IS_LOCATION_FIX_KEY) ;

        //this.isTrackingRequested = savedInstanceState.getBoolean(TRACKING_REQUESTED_STATUS_KEY) ;
        //this.trackingInterval = savedInstanceState.getInt(TRACKING_INTERVAL_KEY) ;


        if(this.isLocationFixObtained == true) {

            // use this if you just want to reset known location but not do anthing on web view

            // locationAPI.setCurrentLocation(this.currentLocation);

            //use below to reset last known location and callback the web view javascript method onLocationFix
            locationAPI.onLocationFix(this.currentLocation);
        }


    }


    @Override
    protected void onDestroy() {

        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy()") ;

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        Log.d(LOG_TAG, "onCreateOptionsMenu: tracking: " + this.isTrackingRequested + " updates:" + this.isUpdatesRequested) ;

        getMenuInflater().inflate(R.menu.menu_web_view_map, menu);
        if(this.isUpdatesRequested)
        {
            MenuItem item = menu.findItem(R.id.action_toggle_location_updates) ;
            item.setIcon(R.drawable.ic_action_location_off) ;
            item.setTitle(R.string.action_stop_location_updates) ;
        }

        if(this.isTrackingRequested)
        {
            MenuItem item = menu.findItem(R.id.action_toggle_request_tracking) ;
            item.setIcon(R.drawable.ic_action_directions_off) ;
            item.setTitle(R.string.action_stop_tracking) ;
        }

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

            locationAPI.requestLocationFix();
            return true ;
        }
        else if(id == R.id.action_toggle_location_updates)
        {
            if(this.isUpdatesRequested==false) {

                locationAPI.requestLocationUpdates(1000);
                item.setIcon(R.drawable.ic_action_location_off) ;
                item.setTitle(R.string.action_stop_location_updates) ;
                this.isUpdatesRequested = true ;
            }
            else
            {
                locationAPI.stopLocationUpdates();
                item.setIcon(R.drawable.ic_action_location_searching) ;
                item.setTitle(R.string.action_start_location_updates) ;
                this.isUpdatesRequested = false ;
            }

            // this.isUpdatesRequested = locationAPI.isUpdatesRequested() ;
            return true ;
        }
        else if(id == R.id.action_toggle_request_tracking)
        {

            if(this.isTrackingRequested == false) {
                locationAPI.startTrackUpdates(3000, "mytrack");
                item.setIcon(R.drawable.ic_action_directions_off) ;
                item.setTitle(R.string.action_stop_tracking) ;
                this.isTrackingRequested = true ;
            }
            else
            {
                locationAPI.stopTrackUpdates();
                item.setIcon(R.drawable.ic_action_directions) ;
                item.setTitle(R.string.action_start_tracking) ;
                this.isTrackingRequested = false ;
                Toast.makeText(this,"Stopping location tracking...", Toast.LENGTH_SHORT).show() ;
            }


            return true;

        }



        return super.onOptionsItemSelected(item);
    }


    public WebView getWebView()
    {

        return this.webView ;
    }
}
