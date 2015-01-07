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
    LocationAPI locationAPI = null ;
    static SharedPreferences location_api_state_prefs ;
    static SharedPreferences.Editor location_api_state_editor ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Log.d("WebViewMap", "onCreate") ;

        // Open Shared Preferences
        location_api_state_prefs = getSharedPreferences("LOCATION_API_STATE", Context.MODE_PRIVATE);

        // Get an editor
        location_api_state_editor = location_api_state_prefs.edit();
        // reset location api status?
//        location_api_state_editor.putInt(LocationAPI.KEY_CURRENT_UPDATE_STATUS, LocationAPI.UPDATES_NOT_INITIALISED);
  //      location_api_state_editor.commit();

        this.webView = new WebView(this) ;
        setContentView(this.webView);

        WebSettings webSettings = webView.getSettings() ;
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setBlockNetworkImage(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadsImagesAutomatically(true);

        webView.addJavascriptInterface(locationAPI, "Android");
        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("file:///android_asset/html/map.html");
            }
        });


    }

    protected void onPause(){
        super.onPause();
        Log.d("WebViewMap", "onPause") ;

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("WebViewMap", "onStop") ;

        this.locationAPI.removeBackgroundLocationUpdates();
        Log.d("WebViewMap", "onStop: removed Background Location Updates") ;


        this.locationAPI.getApiClient().disconnect();
        Log.d("WebViewMap", "onStop: discconected api client") ;

  //      this.locationAPI.unregisterUpdateReceiver();
  //      Log.d("WebViewMap", "onStop: unregistered receiver") ;

   /*
        if(this.locationAPI == null || this.locationAPI.getUpdateReceiver() == null) // update receiver is gone so remove location updates
        {
            Log.d("WebViewMap", "onStop: updateBroadcastReceiver is null so remove location updates"  ) ;
            this.locationAPI.removeBackgroundLocationUpdates();
        }
   */

    }

    @Override
    protected void onStart() {
        super.onStart();
        // create new LocationAPI instance - will request connection to GoogleAPI client
        this.locationAPI = new LocationAPI(this) ;
        // register update receiver
        this.locationAPI.registerUpdateReceiver();

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("WebViewMap", "onRestart()") ;

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d("WebViewMap", "onSavedInstanceState") ;

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d("WebViewMap", "onRestoreInstanceState") ;

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("WebViewMap", "onResume") ;

        if(this.locationAPI == null)
        {
            Log.d("WebViewMap", "onResume locationAPI is null" ) ;
        }
        else {

            Log.d("WebViewMap", "onResume locationAPI is STILL valid" ) ;

            if (!this.locationAPI.getApiClient().isConnected()) {
                Log.d("WebViewMap", "onResume locationAPI is NOT connected");
            } else {
                Log.d("WebViewMap", "onResume locationAPI is STILL connected");
            }


            if(this.locationAPI.getUpdateReceiver() == null)
            {
                Log.d("WebViewMap", "onResume locationAPI update receiver instance is null");
            }
            else
            {
                Log.d("WebViewMap", "onResume locationAPI update receiver instance is STILL valid:" + this.locationAPI.getUpdateReceiver() );
            }
        }

       /*
        if(this.locationAPI == null) {

            Log.d("WebViewMap", "onResume locationAPI is null - will create a new one" ) ;
            this.locationAPI = new LocationAPI(this) ;

        }
        else
        {
            if( ! this.locationAPI.getApiClient().isConnected())
            {
                Log.d("WebViewMap", "onResume locationAPI is not connected...re-connecting" ) ;

                this.locationAPI.getApiClient().connect();

            }
            else
            {
                Log.d("WebViewMap", "onResume locationAPI is still connected" ) ;
            }
        }



        if(this.locationAPI != null && this.locationAPI.getUpdateReceiver() == null) // update receiver is gone so remove location updates
        {
            Log.d("WebViewMap", "onStop: updateBroadcastReceiver is null so remove location updates"  ) ;
            this.locationAPI.removeBackgroundLocationUpdates();
        }



        Log.d("WebViewMap", "onResume currentUpdateStatus: " + this.locationAPI.getCurrentUpdateStatus() ) ;
        this.locationAPI.setCurrentUpdateStatus(currentUpdateStatus) ;
        Log.d("WebViewMap", "onResume reset currentUpdateStatus: " + currentUpdateStatus ) ;
         */

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        Log.d("WebViewMap", "onDestroy") ;
         locationAPI.unregisterUpdateReceiver();

/*
        if(this.locationAPI == null || this.locationAPI.getUpdateReceiver() == null) // update receiver is gone so remove location updates
        {
            Log.d("WebViewMap", "onStop: updateBroadcastReceiver is null so remove location updates"  ) ;
            this.locationAPI.removeBackgroundLocationUpdates();
        }
*/

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
                    webView.loadUrl("javascript:getLocation();");
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
                    webView.loadUrl("javascript:requestBackgroundLocationUpdates();");
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
