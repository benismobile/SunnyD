package org.fabeo.benbutchart.webmap;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by benbutchart on 13/01/2015.
 */
public class TrackingDataSQLiteOpenHelper extends SQLiteOpenHelper {


    private static final int DATABASE_VERSION = 9;
    private static final String DATABASE_NAME = "TRACKING";
    private static final String LOG_TAG = "TrackingDataSQLiteOpenHelper" ;

    TrackingDataSQLiteOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(LOG_TAG, "onCreate") ;
        db.execSQL("CREATE TABLE Tracks(trackid TEXT PRIMARY KEY, trackdata TEXT)");
        db.execSQL("CREATE TABLE LocationUpdates(updateid INTEGER PRIMARY KEY, created_at DATETIME DEFAULT CURRENT_TIMESTAMP, trackid INTEGER, location TEXT, " +
                "FOREIGN KEY(trackid) REFERENCES Tracks(trackid))")  ;

        Log.d(LOG_TAG, "Created tables Tracks and LocationUpdates") ;

        ContentValues values = new ContentValues(2) ;
        values.put("trackid","currenttrack");
        values.put("trackdata","{}");

        db.insert("Tracks",null,values) ;

        Log.d(LOG_TAG, "Inserted track") ;


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        Log.w(LOG_TAG, " upgrade from " + oldVersion + " to " + newVersion) ;

        // TODO archive data in backup tables before dropping tables
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + "LocationUpdates");
        db.execSQL("DROP TABLE IF EXISTS " + "Tracks");


        // Create tables again
        onCreate(db);
    }
}
