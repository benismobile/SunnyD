package org.fabeo.benbutchart.webmap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by benbutchart on 13/01/2015.
 */
public class TrackingDataSQLiteOpenHelper extends SQLiteOpenHelper {


    private static final int DATABASE_VERSION = 17;
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


    public String[] listTracks()
    {

        SQLiteDatabase db = getReadableDatabase() ;

        // Check if track already exists
        String[] query1columns = {"trackid"};

        Cursor cursor = db.query("Tracks", query1columns, null, null, null, null, null);
        int numRows = 0 ;
        String[] tracks =  null ;

        if (cursor == null) {
            db.close();
            return null;
        }
        else
        {
            numRows = cursor.getCount();
            tracks =  new String[numRows] ;
        }

        int i = 0 ;
        if(cursor.moveToFirst()) {
            do {

                String trackdata = cursor.getString(0);
                tracks[i++] = trackdata;
            } while (cursor.moveToNext());
        }
         cursor.close();
         db.close();
         return tracks ;

    }

    public void deleteTrack(String trackid)
    {
        Log.d(LOG_TAG, "delete track " + trackid ) ;
        SQLiteDatabase db = getWritableDatabase() ;

        db.beginTransaction();
        try {
            int numrows = db.delete("LocationUpdates", "trackid=?", new String[]{trackid});
            Log.d(LOG_TAG, "deleted " + numrows + " from LocationUpdates");
            numrows = db.delete("Tracks", "trackid=?", new String[]{trackid});
            Log.d(LOG_TAG, "deleted " + numrows + " from Tracks");
            if(numrows > 0)
            {
                db.setTransactionSuccessful();
            }
        }finally
        {
            db.endTransaction();
            db.close();
        }


    }


    public String getTrackData(String trackid)
    {

        SQLiteDatabase db = getReadableDatabase() ;
        String[] querycolumn = {"trackdata"};
        String selection = "trackid = '" + trackid + "'";

        Cursor cursor = db.query("Tracks", querycolumn, selection, null, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
        }

        String trackdata = cursor.getString(0);
        cursor.close();
        db.close();
        return trackdata ;
    }



}
