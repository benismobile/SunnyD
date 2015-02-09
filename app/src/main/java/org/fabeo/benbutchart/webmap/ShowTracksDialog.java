package org.fabeo.benbutchart.webmap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by benbutchart on 06/02/2015.
 */
public class ShowTracksDialog extends DialogFragment {

    public final String LOG_TAG = "ShowTracksDialog" ;
    private TrackDialogListener listener ;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = (TrackDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement TrackDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {


        final TrackingDataSQLiteOpenHelper dbHelper = new TrackingDataSQLiteOpenHelper(this.getActivity());
        final String[] tracks = dbHelper.listTracks();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select Track")
                .setItems( tracks != null ? tracks : new String[]{"No tracks found"} , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (tracks == null) {


                            Log.d(LOG_TAG, " No tracks retrieved") ;
                        }
                        else {
                            Log.d(LOG_TAG , " item selected: "  + tracks[which]);
                            String trackJSON = dbHelper.getTrackData(tracks[which]);
                            listener.onShowTrack(ShowTracksDialog.this,trackJSON);

                        }
                      }
                });

        return builder.create();
    }
}