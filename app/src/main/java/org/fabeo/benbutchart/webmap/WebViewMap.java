package org.fabeo.benbutchart.webmap;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
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


public class WebViewMap extends Activity {

    WebView webView = null ;
    LocationAPI locationAPI = null ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        this.webView = new WebView(this) ;
        setContentView(this.webView);

        WebSettings webSettings = webView.getSettings() ;
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setBlockNetworkImage(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadsImagesAutomatically(true);

        this.locationAPI = new LocationAPI(this) ;
        webView.addJavascriptInterface(locationAPI, "Android");
        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("file:///android_asset/html/map.html");
            }
        });


    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("WebViewMap", "onStop") ;


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("WebViewMap", "onDestroy") ;

        if (locationAPI != null) {
            locationAPI.removeLocationUpdates();

        }


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
