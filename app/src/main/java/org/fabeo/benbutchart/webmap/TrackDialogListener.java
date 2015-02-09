package org.fabeo.benbutchart.webmap;

import android.app.DialogFragment;

/**
 * Created by benbutchart on 06/02/2015.
 */
public interface TrackDialogListener {


        public void onCreateTrackPositiveClick(DialogFragment dialog,String trackId, int interval);
        public void onCreateTrackNegativeClick(DialogFragment dialog);
        public void onShowTrack(DialogFragment dialog, String trackdata) ;
        public void onDeleteTrack(DialogFragment dialog, String trackId) ;



}
