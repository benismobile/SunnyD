package org.fabeo.benbutchart.webmap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

/**
 * Created by benbutchart on 03/02/2015.
 */
public class CreateTrackDialog extends DialogFragment {

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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();


        View createDialogView = inflater.inflate(R.layout.create_track_dialog,null) ;
        final NumberPicker intervalPicker = (NumberPicker) createDialogView.findViewById(R.id.intervalPicker) ;
        final EditText trackIdView = (EditText) createDialogView.findViewById(R.id.track_id_text) ;
        java.util.Date dte = new java.util.Date() ;
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy h:mm:ss a");
        String formattedDate = sdf.format(dte) ;


        trackIdView.setText("track_"+formattedDate);
        intervalPicker.setMinValue(1);
        intervalPicker.setMaxValue(10);
        intervalPicker.setValue(5);
        intervalPicker.setWrapSelectorWheel(true);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(createDialogView)
                // Add action buttons
                .setPositiveButton("Start", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // get entered track id and start tracking
                        int interval = intervalPicker.getValue() ;
                        String trackId = trackIdView.getText().toString() ;
                        listener.onCreateTrackPositiveClick(CreateTrackDialog.this, trackId, interval);

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        CreateTrackDialog.this.getDialog().cancel();
                        listener.onCreateTrackNegativeClick(CreateTrackDialog.this);

                    }
                });

        return builder.create();

    }

}
