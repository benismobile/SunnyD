package org.fabeo.benbutchart.webmap;

import android.content.Context;
import android.location.Location;

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

        } else {

            // Otherwise, return the empty string
            return "";
        }
    }

}
