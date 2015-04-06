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
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.echo.holographlibrary.Line;
import com.echo.holographlibrary.LineGraph;
import com.echo.holographlibrary.LineGraph.OnPointClickedListener;
import com.echo.holographlibrary.LinePoint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import static org.fabeo.benbutchart.webmap.R.*;


public class LineFragment extends Fragment {

    private static final String LOG_TAG = "LineFragment";
    private Line l ;

    String red = "#FFFF0000" ;
    String blue = "#FF0000FF" ;
    String transparent_blue = "#800000FF" ;
    String green = "#FF00FF00" ;
    String green_light = "#FF99CC00" ;
    String orange = "#FFFFBB33" ;
    String transparent_orange = "#80FFBB33" ;
    String purple = "#FFAA66CC" ;
    String[] colors = {red,blue,  green,green_light,orange,  purple} ;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(LOG_TAG, "onAttach") ;
        MainActivity mainActivity = (MainActivity) activity ;
        mainActivity.setLineFragment(this)  ;

    }

    public void updateChart()
    {
        // Log.d(LOG_TAG, "updateChart") ;
        //l.addPoint(p);
        View v =  this.getView() ;
        LineGraph li = (LineGraph) v.findViewById(id.linegraph);
        li.setRangeY(-130, -20);
        li.setRangeX(-30, 20);


         IODetectorSQLiteOpenHelper dbHelper = new IODetectorSQLiteOpenHelper(this.getActivity()) ;

        List cellInfo = dbHelper.collateCellInfo(null, -30) ;

        // remove all lines - we'll create new ones for each cell
        li.removeAllLines();
        int cellInfoIndex = 0 ;

         for(Iterator<String> i = cellInfo.iterator() ; i.hasNext();)
         {
             String cellInfoStr = i.next() ;
             cellInfoIndex++ ;
             int colorIndex = 0 ;
             if(cellInfoIndex < colors.length ) colorIndex = cellInfoIndex ;

             Line l = new Line();


             try
             {
                  JSONObject cellInfoJSON = new JSONObject(cellInfoStr) ;
                  JSONArray strengths = cellInfoJSON.getJSONArray("strengths") ;

                   for (int strengthsIndex = 0 ; strengthsIndex < strengths.length() ; strengthsIndex++) {
                       int strength = strengths.getInt(strengthsIndex);
                       int cellId = cellInfoJSON.getInt("cellId") ;
                       int minT = cellInfoJSON.getInt("minT") ;
              //         Log.d(LOG_TAG, "parsed cellInfoJSON for cellId " + cellId + " strength:" + strength ) ;
                       LinePoint p = new LinePoint();
                       p.setColor(Color.parseColor(colors[colorIndex])) ;

                       p.setX(minT + strengthsIndex);
                       p.setY(strength);

                      // p.setColor(Color.parseColor(red)) ;
                      // p.setSelectedColor(Color.parseColor(transparent_blue));
                       l.addPoint(p);
                   }
             } catch (JSONException e) {
                    Log.e(LOG_TAG, "Could not parse collated cellinfo object: " + e.getMessage() + " JSON input:" + cellInfoStr) ;
                    throw new IllegalArgumentException("Could not parse collated CellInfo JSON") ;
                }
             l.setColor(Color.parseColor(colors[colorIndex]));
             li.addLine(l);


         }

       // int newPositionX =  new Random().nextInt(100);
       // int newPositionY = new Random().nextInt(200) ;
       // p.setX(newPositionX);
       // p.setY(newPositionY);

     //   li.setLineToFill(0);
        // LinePoint[] newPoints = {p} ;
       // li.addPointsToLine(0,newPoints );
       // li.addPointToLine(0, p);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreateView") ;


        final View v = inflater.inflate(layout.linegraph, container, false);
        final Resources resources = getResources();

/*


        l = new Line();
        l.setUsingDips(true);
        LinePoint p = new LinePoint();
        p.setX(0);
        p.setY(5);

        p.setColor(Color.parseColor(red)) ;
        p.setSelectedColor(Color.parseColor(transparent_blue));
        l.addPoint(p);
        p = new LinePoint();
        p.setX(8);
        p.setY(8);
        p.setColor(Color.parseColor(blue));
        l.addPoint(p);
        p = new LinePoint();
        p.setX(10);
        p.setY(4);
        l.addPoint(p);
        p.setColor(Color.parseColor(green));
        l.setColor(Color.parseColor(orange));

        LineGraph li = (LineGraph) v.findViewById(id.linegraph);
        li.setUsingDips(true);
        li.addLine(l);
        li.setRangeY(-100, 200);
        li.setRangeX(0, 100);
        li.setLineToFill(0);
*/



        return v;
    }

}
