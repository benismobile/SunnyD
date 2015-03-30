package org.fabeo.benbutchart.webmap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Parcel;
import android.telephony.CellInfo;
import android.util.Log;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by benbutchart on 30/03/2015.
 */
public class IODetectorSQLiteOpenHelper extends SQLiteOpenHelper {


    private static final int DATABASE_VERSION = 12;
    private static final String DATABASE_NAME = "IODetector";
    private static final String LOG_TAG = "IODetectorSQLiteOpenHelper" ;

    public IODetectorSQLiteOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        Log.d(LOG_TAG, "create table CellInfo") ;

        db.execSQL("CREATE TABLE CellInfo(timeT INTEGER, cellId INTEGER, strength INTEGER NOT NULL," +
                "PRIMARY KEY(timeT,cellId) )" );

        Log.d(LOG_TAG, "created table CellInfo") ;

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        Log.w(LOG_TAG, " upgrade from " + oldVersion + " to " + newVersion) ;
        db.execSQL("DROP TABLE IF EXISTS " + "CellInfo");
        // Create tables again
        onCreate(db);

    }


    public List<String> listCellInfo()
    {
        SQLiteDatabase db = getReadableDatabase() ;
        String[] query1columns = {"timeT","cellId", "strength"};
        Cursor cursor = db.query("CellInfo", query1columns, null, null, null, null, null);
        int numRows = 0 ;

        ArrayList<String> cellList ;

        if (cursor == null) {
            db.close();
            return null;
        }
        else
        {
            numRows = cursor.getCount();
            cellList = new ArrayList<String>(numRows) ;
        }

        int i = 0 ;
        if(cursor.moveToFirst()) {
            do {

                String timeT = cursor.getString(0);
                String cellId = cursor.getString(1);
                String strength = cursor.getString(2);
                String cellJSON = "{timeT:" + timeT + ", cellId:" + cellId + ", strength:" + strength + "}";
                cellList.add(cellJSON);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return cellList ;

    }

    public int getMaxTimeT()
    {
        SQLiteDatabase db = getReadableDatabase() ;
        String[] query1columns = {"MAX(timeT)"};
        Cursor cursor = db.query("CellInfo", query1columns, null, null, null, null, null);

        int maxT = -1 ;

        if (cursor == null) {
            db.close();
            return maxT;
        }

        if(cursor.moveToFirst())
        {
            maxT = cursor.getInt(0);

        }

        cursor.close();
        db.close();

        return maxT ;

    }

    public void decrementTimeT() throws SQLException
    {
        SQLiteDatabase db = getWritableDatabase() ;
        // decrement timet
        db.execSQL("UPDATE CellInfo SET timeT = timeT - 1");
        db.close();
    }

    public long insertCellInfo(int timet, int cellid, int strength) throws SQLException
    {
        SQLiteDatabase db = getWritableDatabase() ;
        ContentValues values = new ContentValues(3);
        //values.putNull("updateid"); // autoincrement
        values.put("timeT", timet);
        values.put("cellId", cellid);
        values.put("strength", strength);
        // insert new row
        long numrows = db.insertOrThrow("CellInfo", null, values);

        db.close();
        return numrows ;
    }
}
