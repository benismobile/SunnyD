package org.fabeo.benbutchart.webmap;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by benbutchart on 02/12/2014.
 * Utility class for handling location client constants and formatting
 */
public class LocationUtils {


    /*
    * Constants for location update parameters
    */
    // Milliseconds per second
    public static final int MILLISECONDS_PER_SECOND = 1000;

    // The update interval
    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;

    // A fast interval ceiling
    public static final int FAST_CEILING_IN_SECONDS = 1;

    // Update interval in milliseconds
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;

    // A fast ceiling of update intervals, used when the app is visible
    public static final long FAST_INTERVAL_CEILING_IN_MILLISECONDS =
            MILLISECONDS_PER_SECOND * FAST_CEILING_IN_SECONDS;

    public static final String LOCATION_UPDATE_ACTION = "org.fabeo.benbutchart.webmap.LOCATION_UPDATE" ;
    private static final String LOG_TAG = "LocationUtils" ;

    /**
     * Get the latitude and longitude from the Location object returned by
     * Location Services.
     *
     * @param currentLocation A Location object containing the current location
     * @return The latitude and longitude of the current location, or null if no
     * location is available.
     */
    public static String getLatLng(Location currentLocation) {
        // If the location is valid
        if (currentLocation != null) {

            // Return the latitude and longitude as strings
            return
                    "lat:"  + currentLocation.getLatitude() +
                     " longitude:" +  currentLocation.getLongitude();
        } else {

            // Otherwise, return the empty string
            return "";
        }
    }

    /**
     * Get the latitude and longitude from the Location object returned by
     * Location Services as a JSON object.
     *
     * @param currentLocation A Location object containing the current location
     * @return The latitude and longitude of the current location, or null if no
     * location is available.
     */
    public static String getLatLngJSON(Location currentLocation) {
        // If the location is valid
        if (currentLocation != null) {


            return "{ \"lat\":" + currentLocation.getLatitude() + " , \"lon\":" +    currentLocation.getLongitude() + "}" ;
            //TODO add other location properties - geoJSON?
        } else {

            // Otherwise, return the empty string
            return "";
        }
    }

    public static Location getLocationFromJSON(String JSON, String locationProvider)
    {
        try {
            JSONObject json = new JSONObject(JSON);
            double latitude = json.getDouble("lat");
            double longitude = json.getDouble("lat") ;
            Location location = new Location(locationProvider) ;
            location.setLongitude(longitude);
            location.setLatitude(latitude);
            // TODO add other location object properties
            return location ;
        }catch (JSONException e)
        {
            Log.e(LOG_TAG, "error converting JSON String into Location object. JSON passed was " + JSON) ;
            throw new IllegalArgumentException("Error converting JSON String into Android Location object") ;
        }


    }

}
