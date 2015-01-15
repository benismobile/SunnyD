package org.fabeo.benbutchart.webmap;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class LocationUpdateIntentService extends IntentService {

    // Intent actions
    public static final String ACTION_LOCATION_UPDATE = "org.fabeo.benbutchart.webmap.action.LOCATION_UPDATE";
    private static final String LOG_TAG = "LocationUpdateIntentService" ;
    private SQLiteOpenHelper sqLiteHelper ;

    public LocationUpdateIntentService() {

        super("LocationUpdateIntentService");

    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate") ;
        this.sqLiteHelper = new TrackingDataSQLiteOpenHelper(this) ;

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();

            if (ACTION_LOCATION_UPDATE.equals(action)) {
                final Location updateLocation = (Location) intent.getExtras().get(LocationServices.FusedLocationApi.KEY_LOCATION_CHANGED);
                String trackid = (String) intent.getExtras().get("trackid");
                handleLocationUpdate(updateLocation, trackid);
            }
        }
    }

    /**
     * Handle action LOCATION_UPDATE in the provided background thread
     * We expect this to be called via a PendingIntent provided to FusedLocationProvider.requestLocationUpdates method
     */
    private void handleLocationUpdate(Location location, String trackid) {

        Log.d(LOG_TAG, "handleLocationUpdate: for track " + trackid + " location:" + location) ;
        SQLiteDatabase db = sqLiteHelper.getWritableDatabase() ;

        // Check if track already exists
        String[] query1columns = {"trackid"} ;
        String selection = "trackid = '" + trackid +"'";
        Cursor cursor = db.query("Tracks", query1columns, selection, null, null, null, null ) ;
        int numRows = cursor.getCount() ;
        cursor.close();
        if(numRows > 0) {
            Log.d(LOG_TAG, "handleLocationUpdate: track'" + trackid + "' already exists");
        }
        else
        {
            // Create new (empty) track
            Log.d(LOG_TAG, "handleLocationUpdate: track '" + trackid + "' not created yet. insert new row");
            ContentValues trackValues = new ContentValues(2);
            trackValues.put("trackid", trackid);
            String emptyTrack = createEmptyGeoJSONLineString() ;
            trackValues.put("trackdata", emptyTrack) ;
            db.insert("Tracks", null, trackValues) ;
        }

        // insert new update to LocationUpdates for the track with new point
        String pointJSON = this.getLocationAsGeoJSONPoint(location) ;
        ContentValues values = new ContentValues(3) ;
        values.put("updateid",1); // TODO autoincrement
        values.put("trackid", trackid);
        values.put("location",pointJSON);
        //TODO add timestamp to LocationUpdates
        db.insert("LocationUpdates", null, values) ;


        // check the number of updates for the specified track
        String[] query2columns = {"location"} ;
        cursor = db.query("LocationUpdates", query2columns,selection,null,null,null,null) ;
        int numupdates = cursor.getCount() ;
        cursor.close();
        Log.d(LOG_TAG, "number of updates for track '" + trackid + "' is " + numupdates) ;


        String[] query3columns = {"trackdata"} ;
        cursor = db.query("Tracks", query3columns, selection, null, null, null, null) ;
        if (cursor != null)
        { cursor.moveToFirst(); }
        String trackdata = cursor.getString(0) ;
        cursor.close();
        String updatedTrackData = this.appendLocationToTrack(trackdata, location) ;

        // now update the entry for Tracks with accumulated trackdata

        ContentValues updateValues = new ContentValues(2) ;
        updateValues.put("trackid" , trackid);
        updateValues.put("trackdata", updatedTrackData) ;
        db.update("Tracks" ,updateValues,  selection, null) ;

        db.close();
    }


    private String createEmptyGeoJSONLineString()
    {

        JSONObject geoJSON = new JSONObject();

        try {
            JSONArray coordinatesArray = new JSONArray();
            JSONObject geometry = new JSONObject();
            geometry.put("type", "LineString");
            geometry.put("coordinates", coordinatesArray);

            geoJSON.put("type", "Feature");
            geoJSON.put("geometry", geometry);
        }catch(JSONException e)
        {
            Log.e(LOG_TAG, "createEmptyJSONString failed: " + e ) ;
            throw new IllegalStateException("createEmptyJSONString failed ", e) ;
        }

        return geoJSON.toString() ;


/*
        "type": "Feature",
            "geometry": {
        "type": "LineString",
                "coordinates": [
        [
        100,
                0
        ],
        [
        101,
                1
        ]
        ]
    }
    }
  */
    }

    private String getLocationAsGeoJSONPoint(Location location)
    {
        JSONObject geoJSON = new JSONObject() ;

        try {
            JSONArray coordinatesArray = new JSONArray();
            coordinatesArray.put(location.getLongitude()) ;
            coordinatesArray.put(location.getLatitude()) ;
            JSONObject geometry = new JSONObject();
            geometry.put("type", "Point");
            geometry.put("coordinates", coordinatesArray);
            geoJSON.put("type", "Feature");
            geoJSON.put("geometry", geometry);

        }catch(JSONException e)
        {
            Log.e(LOG_TAG, "createEmptyJSONString failed: " + e ) ;
            throw new IllegalStateException("createEmptyJSONString failed ", e) ;
        }

    }

    private String appendLocationToTrack(String trackdata, Location location)
    {
        JSONObject geoJSON ;
        try {
                geoJSON = new JSONObject(trackdata);
                JSONObject geometry = geoJSON.getJSONObject("geometry");
                JSONArray coordinatesArray = geometry.getJSONArray("coordinates") ;
                Log.d(LOG_TAG, "number of points in track before: " + coordinatesArray.length()) ;
                JSONArray coordinates = new JSONArray();
                coordinates.put(location.getLongitude());
                coordinates.put(location.getLatitude());
                coordinatesArray.put(coordinates) ;

        }catch(JSONException e)
        {
            Log.e(LOG_TAG, "Could not add Location " + location + " to track " + e) ;
            throw new IllegalArgumentException("Could not add location to track", e) ;
        }
        return geoJSON.toString() ;
    }

}
