/*
 * 	   Created by Daniel Nadeau
 * 	   daniel.nadeau01@gmail.com
 * 	   danielnadeau.blogspot.com
 *
 * 	   Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */

package org.fabeo.benbutchart.webmap;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.astuetz.PagerSlidingTabStrip;
import com.google.android.gms.location.LocationServices;

import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class MainActivity extends FragmentActivity {


    // TODO find solution to color resoruces issue so can avoid below
    String red = "#FFFF0000" ;
    String blue = "#FF0000FF" ;
    String transparent_blue = "#800000FF" ;
    String green = "#FF00FF00" ;
    String green_light = "#FF99CC00" ;
    String orange = "#FFFFBB33" ;
    String transparent_orange = "#80FFBB33" ;
    String purple = "#FFAA66CC" ;
    final String LOG_TAG = "MainActivity" ;

    CellInfoUpdateReceiver cellInfoReceiver ;

    public void setLineFragment(LineFragment lineFragment) {
        this.lineFragment = lineFragment;
    }

    LineFragment lineFragment ;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Get the ViewPager and set it's PagerAdapter so that it can display items

        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(new PagerAdapter(getSupportFragmentManager()));
      /*
        viewPager.getAdapter().registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();

            }
        });

        */
        // Give the PagerSlidingTabStrip the ViewPager
        PagerSlidingTabStrip tabsStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabsStrip.setIndicatorColor(Color.parseColor(blue));
        tabsStrip.setAllCaps(false);
        tabsStrip.setShouldExpand(true);
        tabsStrip.setUnderlineHeight(1);
        tabsStrip.setEnabled(true);
        tabsStrip.setBackgroundColor(Color.parseColor(orange));
        //tabsStrip.setUnderlineColor(Color.parseColor(blue));
       // tabsStrip.setDividerColor(Color.parseColor(green));
        // Attach the view pager to the tab strip
        tabsStrip.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
                Log.d(LOG_TAG, "onPageScrolled:" + i) ;
            }

            @Override
            public void onPageSelected(int i) {
                Log.d(LOG_TAG, "onPageSelected:" + i) ;
            }


            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        tabsStrip.setViewPager(viewPager);

        WakefulBroadcastReceiver receiver = new WakefulAlarmBroadcastReceiver() ;
        this.registerReceiver(receiver, new IntentFilter("org.fabeo.benbutchart.webmap.ACTION_UPDATE_CELL_INFO"));
        Intent intent = new Intent(this, WakefulAlarmBroadcastReceiver.class);
        intent.setAction("org.fabeo.benbutchart.webmap.ACTION_UPDATE_CELL_INFO") ;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 992, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager manager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        long timeInterval = 60 * 1000 ;
        manager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), timeInterval, pendingIntent);
        Log.d(LOG_TAG, "Alarm set") ;

        this.cellInfoReceiver = new CellInfoUpdateReceiver() ;

        registerCellInfoReceiver();
        viewPager.getAdapter().registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                /*
                IODetectorSQLiteOpenHelper dbHelper = new IODetectorSQLiteOpenHelper(this) ;
                List cellInfo = dbHelper.getCellInfo(0) ;

                for(Iterator<String> i = cellInfo.iterator() ; i.hasNext();)
                {
                    String cellInfoStr = i.next() ;

                    JSONObject cellInfoJSON = new JSONObject(cellInfoStr) ;
                    int strength = cellInfoJSON.getInt("stength") ;

                }
*/
            }

        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerCellInfoReceiver();
    }

    private void registerCellInfoReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(IODetectorService.CELL_INFO_ACTION);

        if (this.cellInfoReceiver != null) {
            this.registerReceiver(this.cellInfoReceiver, filter);
            Log.d(LOG_TAG, "registered CellInfoReceiver");
        } else {
            Log.e(LOG_TAG, " Could not register CellInfoReceiver");
        }
    }

    private void unregisterCellInfoReceiver() {

        if (this.cellInfoReceiver == null) {
            this.cellInfoReceiver = new CellInfoUpdateReceiver();

        }

        this.unregisterReceiver(this.cellInfoReceiver);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterCellInfoReceiver();

    }

    public class CellInfoUpdateReceiver extends BroadcastReceiver
    {


        @Override
        public void onReceive(Context context, Intent intent) {


            Log.d(LOG_TAG, "CellInforUpdateReceiver: onReceive called");
            String collatedvalues = (String) intent.getExtras().get(IODetectorService.CELL_INFO) ;
            Log.d(LOG_TAG, "CellInforUpdateReceiver:colllatedvalues:" + collatedvalues ) ;
           //TODO update page views to get latest data
            //ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
            //viewPager.getAdapter().notifyDataSetChanged();

           lineFragment.updateChart() ;

        }

    }


}
